package de.serviceflow.nutshell.cl.test1;

import java.security.Principal;

import de.serviceflow.nutshell.cl.Authentication;

public class TestAuthentication implements Authentication {

	@Override
	public Principal authenticate(byte[] credentials) {
		return new Principal() {
			@Override
			public String getName() {
				return "TestUser";
			}
		};
	}

}
