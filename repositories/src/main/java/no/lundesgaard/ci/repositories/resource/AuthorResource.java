package no.lundesgaard.ci.repositories.resource;

import org.springframework.hateoas.ResourceSupport;

public class AuthorResource extends ResourceSupport {
	private String name;
	private String email;

	public AuthorResource(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
