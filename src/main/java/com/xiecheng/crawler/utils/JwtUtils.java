package com.xiecheng.crawler.utils;

import com.xiecheng.crawler.constant.CommonConstants;
import com.xiecheng.crawler.entity.po.CustomerDO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * @author nijichang
 * @since 2020-11-10 18:10:19
 */
public class JwtUtils {

    /**
     *功能描述: 生成 jwt token
     * @Param [user]
     * @return java.lang.String
     */
    public static String geneJsonWebToken(CustomerDO customer){
        if (customer == null) {
            return null;
        }
        String token = Jwts.builder().setSubject(CommonConstants.JWT_SUBJECT)
                .claim("id", customer.getId())
                .claim("name", customer.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + CommonConstants.JWT_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, CommonConstants.JWT_APPSECRET).compact();
        return token;
    }
    
    /**
     *功能描述: 校验token
     * @Param [token] 
     * @return io.jsonwebtoken.Claims
     */
    public static Claims checkJWT(String token){
        try {
            final Claims claims = Jwts.parser().setSigningKey(CommonConstants.JWT_APPSECRET).parseClaimsJws(token).getBody();
            return claims;
        }catch (Exception e){
            return null;
        }
    }
}
