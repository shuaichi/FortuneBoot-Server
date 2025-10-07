package com.fortuneboot.service.login;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.fortuneboot.common.constant.Constants.Token;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.service.cache.RedisCacheService;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * token验证处理
 *
 * @author valarchie
 */
@Slf4j
@Data
@Component
@RequiredArgsConstructor
public class TokenService {

    /**
     * 自定义令牌标识
     */
    @Value("${token.header}")
    private String header;

    /**
     * 令牌秘钥
     */
    @Value("${token.secret}")
    private String secret;

    /**
     * 过期时间，30天
     */
//    private static final Long EXP_TIME = 20 * 1000L;
    private static final Long EXP_TIME = 24 * 30  * 60 * 60 * 1000L;

    /**
     * 自动刷新token的时间，当过期时间不足autoRefreshTime的值的时候，会触发刷新用户登录缓存的时间
     * 比如这个值是20,   用户是8点登录的， 8点半缓存会过期， 当过8.10分的时候，就少于20分钟了，便触发
     * 刷新登录用户的缓存时间
     */
    @Value("${token.autoRefreshTime}")
    private long autoRefreshTime;

    private final RedisCacheService redisCache;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public SystemLoginUser getLoginUser(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求携带的令牌
        String token = getTokenFromRequest(request);
        if (StrUtil.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Token.LOGIN_USER_KEY);
                SystemLoginUser loginUser = redisCache.loginUserCache.getObjectOnlyInCacheById(uuid);

                // 如果 loginUser 为 null，说明缓存过期或用户登出
                if (loginUser == null) {
                    throw new ApiException(ErrorCode.Client.INVALID_TOKEN);
                }

                // 检查是否需要刷新 JWT（例如：当剩余有效期 <= autoRefreshTime 时刷新）
                long now = System.currentTimeMillis();
                Date exp = claims.getExpiration();
                long remaining = exp.getTime() - now;

                // autoRefreshTime 单位：分钟（你的 @Value 注入），转换为毫秒
                long refreshThresholdMs = TimeUnit.MINUTES.toMillis(autoRefreshTime);

                if (remaining > refreshThresholdMs) {
                    // 生成新的 JWT，并把新的 token 放到响应头，前端需替换掉旧 token
                    String newToken = generateToken(MapUtil.of(Token.LOGIN_USER_KEY, loginUser.getCachedKey()));
                    // 把新的 token 放在响应头里（注意前端需要处理）
                    response.setHeader(header, Token.PREFIX + newToken);
                }

                return loginUser;
            } catch (io.jsonwebtoken.ExpiredJwtException expiredEx) {
                log.warn("token expired.", expiredEx);
                throw new ApiException(expiredEx, ErrorCode.Client.INVALID_TOKEN);
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException |
                     IllegalArgumentException jwtException) {
                log.error("parse token failed.", jwtException);
                throw new ApiException(jwtException, ErrorCode.Client.INVALID_TOKEN);
            } catch (Exception e) {
                log.error("fail to get cached user from redis", e);
                throw new ApiException(e, ErrorCode.Client.TOKEN_PROCESS_FAILED, e.getMessage());
            }

        }
        return null;
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createTokenAndPutUserInCache(SystemLoginUser loginUser) {
        loginUser.setCachedKey(IdUtil.fastUUID());

        redisCache.loginUserCache.set(loginUser.getCachedKey(), loginUser);

        return generateToken(MapUtil.of(Token.LOGIN_USER_KEY, loginUser.getCachedKey()));
    }

    /**
     * 当超过20分钟，自动刷新token
     *
     * @param loginUser 登录用户
     */
    public void refreshToken(SystemLoginUser loginUser) {
        long currentTime = System.currentTimeMillis();
        Long refreshTime = loginUser.getAutoRefreshCacheTime();
        if (currentTime > refreshTime) {
            loginUser.setAutoRefreshCacheTime(currentTime + TimeUnit.MINUTES.toMillis(autoRefreshTime));
            // 根据uuid将loginUser存入缓存
            redisCache.loginUserCache.set(loginUser.getCachedKey(), loginUser);
        }
    }


    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXP_TIME))
                .signWith(key)
                .claims(claims)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    private String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @return token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StrUtil.isNotEmpty(token) && token.startsWith(Token.PREFIX)) {
            token = StrUtil.stripIgnoreCase(token, Token.PREFIX, null);
        }
        return token;
    }

}
