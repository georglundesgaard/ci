package no.lundesgaard.ci.data.simple;

import no.lundesgaard.ci.data.Data;
import no.lundesgaard.ci.data.Repositories;

import java.util.HashMap;

public class SimpleData implements Data {
	private final Repositories repositories = new Repositories(new HashMap<>());

	@Override
	public Repositories repositories() {
		return repositories;
	}
}
