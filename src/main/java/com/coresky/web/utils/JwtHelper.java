package com.coresky.web.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.*;


public class JwtHelper {
	
	static final String SECRET = "coresky-access-token";
	//
	static final String ISSUSER = "coresky-isuser";
	//
	static final String SUBJECT = "this is coresky nft token";
	//
	static final String AUDIENCE = "coresky-audience";

	static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

	public static String createToken(String address){
		try {
		    Algorithm algorithm = Algorithm.HMAC256(SECRET);
		    Map<String, Object> map = new HashMap<>();
		    Date nowDate = new Date();
		    // 有效期 1 个月
		    Date expireDate = getAfterDate(nowDate,0,1,0,0,0,0);
	        map.put("alg", "HS256");
	        map.put("typ", "JWT");
			return JWT.create()
		    	// 设置头部信息 Header
		    	.withHeader(map)
		    	// 设置 载荷 Payload
				.withClaim("address", address)
		        .withIssuer(ISSUSER)
		        .withSubject(SUBJECT)
		        .withAudience(AUDIENCE)
		        // 生成签名的时间
		        .withIssuedAt(nowDate)
		        // 签名过期的时间
		        .withExpiresAt(expireDate)
		        // 签名 Signature
		        .sign(algorithm);
		} catch (JWTCreationException exception){
			exception.printStackTrace();
		}
		return null;
	}

	public static String verifyTokenAndGetUserAddress(String token) {
		try {
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer(ISSUSER)
					.build();
			DecodedJWT jwt = verifier.verify(token);
			Map<String, Claim> claims = jwt.getClaims();
			Claim claim = claims.get("address");
			if (null == claim){
				return null;
			}
			return claim.asString();
		} catch (JWTVerificationException exception){
			return null;
		}
	}

	public static Date getAfterDate(Date date, int year, int month, int day, int hour, int minute, int second){
		if(date == null){
			date = new Date();
		}

		Calendar cal = new GregorianCalendar();

		cal.setTime(date);
		if(year != 0){
			cal.add(Calendar.YEAR, year);
		}
		if(month != 0){
			cal.add(Calendar.MONTH, month);
		}
		if(day != 0){
			cal.add(Calendar.DATE, day);
		}
		if(hour != 0){
			cal.add(Calendar.HOUR_OF_DAY, hour);
		}
		if(minute != 0){
			cal.add(Calendar.MINUTE, minute);
		}
		if(second != 0){
			cal.add(Calendar.SECOND, second);
		}
		return cal.getTime();
	}

}
