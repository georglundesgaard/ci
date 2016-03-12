package no.lundesgaard.ci.repositories.model;

public class Author {
	private String name;
	private String email;

	public Author(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
}
