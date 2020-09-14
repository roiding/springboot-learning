package roi.ding.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import roi.ding.service.MyUserDetailsService;

import java.io.PrintWriter;

/**
 * Description: Security 配置类
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    BackdoorAuthenticationProvider backdoorAuthenticationProvider;
    @Autowired
    MyUserDetailsService myUserDetailsService;
    @Autowired
    MyAccessDecisionManager myAccessDecisionManager;
    @Autowired
    MySecurityMetadataSource mySecurityMetadataSource;
    @Autowired
    MyAccessDeniedHandler myAccessDeniedHandler;

    @Bean
    MyAdditonalAuthenticationProvider myAdditonalAuthenticationProvider(){
        MyAdditonalAuthenticationProvider myAdditonalAuthenticationProvider =new MyAdditonalAuthenticationProvider();
        myAdditonalAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        myAdditonalAuthenticationProvider.setUserDetailsService(myUserDetailsService);
        return myAdditonalAuthenticationProvider;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 发现这样的每一个都会形成一个AuthenticationProvider，一旦登录不成功，报错会是最后一个的错误
        /**
         * 在内存中创建一个名为 "user" 的用户，密码为 "pwd"，拥有 "USER" 权限，密码使用BCryptPasswordEncoder加密
         */
       // auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
         //       .withUser("user").password(new BCryptPasswordEncoder().encode("pwd")).roles("USER");
        /**
         * 在内存中创建一个名为 "admin" 的用户，密码为 "pwd"，拥有 "USER" 和"ADMIN"权限
         */
        // auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
        //        .withUser("admin").password(new BCryptPasswordEncoder().encode("pwd")).roles("USER", "ADMIN");
        //将自定义验证类注册进去
        auth.authenticationProvider(backdoorAuthenticationProvider);
        //加入数据库验证类，下面的语句实际上在验证链中加入了一个DaoAuthenticationProvider
       // auth.userDetailsService(myUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
        auth.authenticationProvider(myAdditonalAuthenticationProvider());

    }
    //实测优先级高于下面
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers( "/vc.jpg","/index.html", "/static/**", "/favicon.ico","/error","/login_p");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setSecurityMetadataSource(mySecurityMetadataSource);
                        object.setAccessDecisionManager(myAccessDecisionManager);
                        return object;
                    }
                })
                .and()
                .formLogin()
                .successHandler((req, resp, authentication) -> { //前后端分离
                    Object principal = authentication.getPrincipal();
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(principal));
                    out.flush();
                    out.close();
                })
                .failureHandler((req, resp, e) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(e.getMessage());
                    out.flush();
                    out.close();
                })
                .loginPage("/login_p") //指定登录页
                .loginProcessingUrl("/login").permitAll() //指定登录接口
                //1.自定义参数名称，与login.html中的参数对应
                .usernameParameter("myusername")
                .passwordParameter("mypassword")
                .defaultSuccessUrl("/index") //转发
                //.successForwardUrl == defaultSuccessUrl("",true) 重定向
                //.failureForwardUrl() //登录失败
                //.failureUrl()
                .and()
                .logout()
                .logoutUrl("/logout")
                //.logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST")) 修改注销 URL，还可以修改请求方式，实际项目中，这个方法和 logoutUrl 任意设置一个即可。
                .logoutSuccessUrl("/login").permitAll()
                //.deleteCookies()
                //.clearAuthentication 清除认证信息 默认就会清除
                //.invalidateHttpSession 使HttpSession失效  默认失效
                .and()
                .csrf().disable()
                .exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);
    }
}
