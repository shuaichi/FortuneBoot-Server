package com.fortuneboot.service.login;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.core.codec.Base64;
import com.fortuneboot.common.config.FortuneBootConfig;
import com.fortuneboot.common.constant.Constants.Captcha;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.exception.error.ErrorCode.Business;
import com.fortuneboot.common.utils.ServletHolderUtil;
import com.fortuneboot.common.utils.i18n.MessageUtils;
import com.fortuneboot.customize.async.AsyncTaskFactory;
import com.fortuneboot.service.cache.GuavaCacheService;
import com.fortuneboot.service.cache.MapCache;
import com.fortuneboot.service.cache.CacheService;
import com.fortuneboot.infrastructure.thread.ThreadPoolManager;
import com.fortuneboot.infrastructure.config.captcha.SvgCaptchaUtil;
import com.fortuneboot.service.login.dto.CaptchaDTO;
import com.fortuneboot.service.login.dto.ConfigDTO;
import com.fortuneboot.service.login.command.LoginCommand;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import com.fortuneboot.common.enums.common.LoginStatusEnum;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 登录校验方法
 *
 * @author ruoyi
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private final TokenService tokenService;

    private final CacheService cacheService;

    private final GuavaCacheService guavaCache;

    private final AuthenticationManager authenticationManager;

    /**
     * 登录验证
     *
     * @param loginCommand 登录参数
     * @return 结果
     */
    public String login(LoginCommand loginCommand) {
        // 验证码开关
        if (isCaptchaOn()) {
            validateCaptcha(loginCommand.getUsername(), loginCommand.getCaptchaCode(), loginCommand.getCaptchaCodeKey());
        }
        // 用户验证
        Authentication authentication;
        String decryptPassword = decryptPassword(loginCommand.getPassword());
        try {
            // 该方法会去调用UserDetailsServiceImpl#loadUserByUsername  校验用户名和密码  认证鉴权
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginCommand.getUsername(), decryptPassword));
        } catch (BadCredentialsException e) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginCommand.getUsername(), LoginStatusEnum.LOGIN_FAIL,
                MessageUtils.message("Business.LOGIN_WRONG_USER_PASSWORD")));
            throw new ApiException(e, ErrorCode.Business.LOGIN_WRONG_USER_PASSWORD);
        } catch (AuthenticationException e) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginCommand.getUsername(), LoginStatusEnum.LOGIN_FAIL, e.getMessage()));
            throw new ApiException(e, ErrorCode.Business.LOGIN_ERROR, e.getMessage());
        } catch (Exception e) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginCommand.getUsername(), LoginStatusEnum.LOGIN_FAIL, e.getMessage()));
            throw new ApiException(e, Business.LOGIN_ERROR, e.getMessage());
        }
        // 把当前登录用户 放入上下文中
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 这里获取的loginUser是UserDetailsServiceImpl#loadUserByUsername方法返回的LoginUser
        SystemLoginUser loginUser = (SystemLoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser);
        // 生成token
        return tokenService.createTokenAndPutUserInCache(loginUser);
    }

    /**
     * 获取验证码 data
     *
     * @return {@link ConfigDTO}
     */
    public ConfigDTO getConfig() {
        ConfigDTO configDTO = new ConfigDTO();

        boolean isCaptchaOn = isCaptchaOn();
        configDTO.setIsCaptchaOn(isCaptchaOn);
        configDTO.setDictionary(MapCache.dictionaryCache());
        return configDTO;
    }

    /**
     * 获取验证码 data
     *
     * @return 验证码
     */
    public CaptchaDTO generateCaptchaImg() {
        CaptchaDTO captchaDTO = new CaptchaDTO();

        boolean isCaptchaOn = isCaptchaOn();
        captchaDTO.setIsCaptchaOn(isCaptchaOn);

        if (isCaptchaOn) {
            SvgCaptchaUtil.CaptchaResult captchaResult;

            // 生成验证码（纯 SVG，不依赖 AWT，兼容 GraalVM native image）
            String captchaType = FortuneBootConfig.getCaptchaType();
            if (Captcha.MATH_TYPE.equals(captchaType)) {
                captchaResult = SvgCaptchaUtil.createMathCaptcha();
            } else if (Captcha.CHAR_TYPE.equals(captchaType)) {
                captchaResult = SvgCaptchaUtil.createCharCaptcha(4);
            } else {
                throw new ApiException(ErrorCode.Internal.LOGIN_CAPTCHA_GENERATE_FAIL);
            }

            // 保存验证码信息
            String imgKey = IdUtil.simpleUUID();
            cacheService.captchaCache.set(imgKey, captchaResult.getAnswer());

            captchaDTO.setCaptchaCodeKey(imgKey);
            captchaDTO.setCaptchaCodeImg(captchaResult.getImageBase64());
        }

        return captchaDTO;
    }


    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param captchaCode 验证码
     * @param captchaCodeKey 验证码对应的缓存key
     */
    public void validateCaptcha(String username, String captchaCode, String captchaCodeKey) {
        String captcha = cacheService.captchaCache.getObjectOnlyInCacheById(captchaCodeKey);
        cacheService.captchaCache.delete(captchaCodeKey);

        if (captcha == null) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(username, LoginStatusEnum.LOGIN_FAIL,
                    ErrorCode.Business.LOGIN_CAPTCHA_CODE_EXPIRE.message()));
            throw new ApiException(ErrorCode.Business.LOGIN_CAPTCHA_CODE_EXPIRE);
        }
        if (!captchaCode.equalsIgnoreCase(captcha)) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(username, LoginStatusEnum.LOGIN_FAIL,
                    ErrorCode.Business.LOGIN_CAPTCHA_CODE_WRONG.message()));
            throw new ApiException(ErrorCode.Business.LOGIN_CAPTCHA_CODE_WRONG);
        }
    }
    /**
     * 记录登录信息
     * @param loginUser 登录用户
     */
    public void recordLoginInfo(SystemLoginUser loginUser) {
        ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginUser.getUsername(), LoginStatusEnum.LOGIN_SUCCESS,
            LoginStatusEnum.LOGIN_SUCCESS.getDescription()));
    
        SysUserEntity entity = cacheService.userCache.getObjectById(loginUser.getUserId());
    
        entity.setLoginIp(JakartaServletUtil.getClientIP(ServletHolderUtil.getRequest()));
        entity.setLoginDate(DateUtil.date());
        entity.updateById();
    }

    public String decryptPassword(String originalPassword) {
        byte[] decryptBytes = SecureUtil.rsa(FortuneBootConfig.getRsaPrivateKey(), null)
            .decrypt(Base64.decode(originalPassword), KeyType.PrivateKey);

        return StrUtil.str(decryptBytes, CharsetUtil.CHARSET_UTF_8);
    }

    private boolean isCaptchaOn() {
        return Convert.toBool(guavaCache.configCache.get(ConfigKeyEnum.CAPTCHA.getValue()));
    }

}