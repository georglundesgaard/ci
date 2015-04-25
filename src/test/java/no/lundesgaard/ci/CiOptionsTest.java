package no.lundesgaard.ci;


import org.junit.Test;

import static no.lundesgaard.ci.model.Type.HAZELCAST;
import static no.lundesgaard.ci.model.Type.SIMPLE;
import static org.assertj.core.api.Assertions.assertThat;


public class CiOptionsTest {
	@Test
	public void constructor_givenRootParameter_setsRootProperty() throws Exception {
		assertThat(new CiOptions("-r", "root").root).isEqualTo("root");
	}

	@Test
	public void constructor_givenRootParameter_setsDefaultTypeProperty() throws Exception {
		assertThat(new CiOptions("-r", "root").type).isEqualTo(SIMPLE);
	}

	@Test
	public void constructor_givenRootParameter_isValidTrue() throws Exception {
		assertThat(new CiOptions("-r", "root").isValid()).isTrue();
	}

	@Test
	public void constructor_givenNoParameters_isValidFalse() throws Exception {
		assertThat(new CiOptions().isValid()).isFalse();
	}

	@Test
	public void constructor_givenRootAndTypeParameter_setsTypeProperty() throws Exception {
		assertThat(new CiOptions("-r", "root", "-t", "hazelcast").type).isEqualTo(HAZELCAST);
	}
}