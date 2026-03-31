package com.fortuneboot.common.utils.ip;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;

import java.io.InputStream;

/**
 * @author valarchie
 */
@Slf4j
public class OfflineIpRegionUtil {

    private static Searcher searcher;

    private OfflineIpRegionUtil() {
    }

    static {
        try (InputStream resourceAsStream = OfflineIpRegionUtil.class.getResourceAsStream("/ip2region.xdb")) {
            assert resourceAsStream != null;
            LongByteArray content = Searcher.loadContentFromInputStream(resourceAsStream);
            searcher = Searcher.newWithBuffer(Version.IPv4, content);
        } catch (Exception e) {
            log.error("构建本地Ip缓存失败", e);
        }
    }

    public static IpRegion getIpRegion(String ip) {
        try {
            if (StrUtil.isBlank(ip) || IpUtil.isValidIpv6(ip)
                || !IpUtil.isValidIpv4(ip)) {
                return null;
            }

            String rawRegion = searcher.search(ip);

            if (StrUtil.isEmpty(rawRegion)) {
                return null;
            }

            String[] split = rawRegion.split("\\|");
            return new IpRegion(split[0], split[1], split[2], split[3], split[4]);

        } catch (Exception e) {
            log.error("获取IP地理位置失败", e);
        }
        return null;
    }


}
