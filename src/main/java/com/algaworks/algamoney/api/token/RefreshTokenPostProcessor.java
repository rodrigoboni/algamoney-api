package com.algaworks.algamoney.api.token;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Modificar response http para tirar refresh token do body e por em cookie
 * 
 * Define cookie seguro (qdo em https) - desta forma o js não consegue acessar o cookie, gerando risco de segurança
 * 
 * O browser sempre envia o cookie junto com o request
 * 
 * Assim o filtro definido p/ pré-processar os requests p/ obter novo token são interceptados e o refresh token 
 * recebido via cookie é adicionado como parâmetro no body do request
 * 
 * @author s2it_rboni
 */
@ControllerAdvice
public class RefreshTokenPostProcessor implements ResponseBodyAdvice<OAuth2AccessToken> {

	/**
	 * Qdo este método retornar true o método beforebodywrite será invocado(filtra qdo o body deve ser modificado)
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.getMethod().getName().equals("postAccessToken"); // interceptar response somente qdo for request do oauth
	}

	/**
	 * Definir cookie e remover o token do response body
	 */
	@Override
	public OAuth2AccessToken beforeBodyWrite(OAuth2AccessToken body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		
		addRefreshTokenCookie(body.getRefreshToken().getValue(), ((ServletServerHttpRequest)request).getServletRequest(), ((ServletServerHttpResponse)response).getServletResponse());

		removeRefreshTokenBody((DefaultOAuth2AccessToken)body);
		
		return body;
	}

	private void addRefreshTokenCookie(String refreshToken, HttpServletRequest request,
			HttpServletResponse response) {
		Cookie cookie = new Cookie("refreshToken", refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(false); //TODO TRUE QDO FOR PRODUÇÃO
		cookie.setPath(request.getContextPath() + "/oauth/token");
		cookie.setMaxAge(2592000); //30 dias
		response.addCookie(cookie);
	}

	private void removeRefreshTokenBody(DefaultOAuth2AccessToken body) {
		body.setRefreshToken(null);
	}
}