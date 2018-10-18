package com.algaworks.algamoney.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

/**
 * Configuração da segurança do servidor de recursos
 *
 * @author s2it_rboni
 */
@Configuration
@EnableWebSecurity
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    // recebe instância da implementação desta interface (lê usuários da base de dados)
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Configura o autorizador de recursos
     *
     * @param auth
     * @throws Exception
     */
    @Autowired
    protected void configure (AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(getPasswordEncoder());

    }

    // retorna codificador de senhas
    @Bean
    public PasswordEncoder getPasswordEncoder () {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define as regra do autorizador de recursos http
     */
    @Override
    public void configure (HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //.antMatchers("/categorias").permitAll() // liberar url - será ignorada se houver anotação de autorização em resource com esta url
                .antMatchers("/v2/api-docs").permitAll() // TODO - REMOVER EM PRODUÇÃO
                .antMatchers("/swagger-ui.html").permitAll() // TODO - REMOVER EM PRODUÇÃO
                .antMatchers("/webjars/*").permitAll() // TODO - REMOVER EM PRODUÇÃO
                .anyRequest().authenticated() // as demais urls devem ser autenticadas
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() // sessões stateless
                .csrf().disable(); // desabilitar csrf (cross site request forgery)
    }

    /**
     * configurar autorizador p/ ser stateless
     */
    @Override
    public void configure (ResourceServerSecurityConfigurer resources) throws Exception {
        resources.stateless(true);
    }

    /**
     * Tratamento de expressões oauth2
     *
     * @return
     */
    @Bean
    public MethodSecurityExpressionHandler createExpressionHandler () {
        return new OAuth2MethodSecurityExpressionHandler();
    }
}

//basic auth

//@Configuration
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication()
//			.withUser("admin").password("admin")
//			.roles("ROLE");
//	}
//
//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests()
//			.antMatchers("/categorias").permitAll()
//			.anyRequest().authenticated()
//			.and()
//			.httpBasic().and()
//			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//			.csrf().disable();
//	}
//}