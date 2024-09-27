package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.Model.UserDtls;
import com.ecom.Repository.UserRepository;
import com.ecom.Service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email =request.getParameter("username");
		
		UserDtls userDtls =userRepository.findByEmail(email);
		
		if(userDtls != null)
		{
		if( userDtls.getIsEnable())
		{
			if(userDtls.getAccountNonLocked())
			{
				if(userDtls.getFailedAttempt()<AppConstant.ATTEMPT_TIME)
				{
					userService.increaseFailedAttempt(userDtls);
				}else {
					userService.userAccountLock(userDtls);
					exception = new LockedException("Your account is locked || failed Attempt");
				}
			}else
			{
				if(userService.unlockAccountTimeExpired(userDtls))
				{
					exception = new LockedException("Your account is unlocked || please try to login!!");
				}else {
					exception = new LockedException("Your account is locked || please try after sometimes");
				}
				
			}
			
		}else {
			
			exception = new LockedException("Your account is inactive");
		}
		}else {
			exception = new LockedException("Your email & password is invalid");
		}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}
	
	
}
