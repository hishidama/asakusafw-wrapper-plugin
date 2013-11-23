package jp.hishidama.eclipse_plugin.asakusafw_wrapper.extension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class AsakusafwConfigurationTest {

	@Test
	public void testContainsVersion() {
		assertThat(AsakusafwConfiguration.containsVersion("0.4", "0.5", "0.5-SNAPSHOT"), is(false));
		assertThat(AsakusafwConfiguration.containsVersion("0.4-SNAPSHOT", "0.5", "0.5-SNAPSHOT"), is(false));
		assertThat(AsakusafwConfiguration.containsVersion("0.5", "0.5", "0.5-SNAPSHOT"), is(true));
		assertThat(AsakusafwConfiguration.containsVersion("0.5.0", "0.5", "0.5-SNAPSHOT"), is(true));
		assertThat(AsakusafwConfiguration.containsVersion("0.5.1", "0.5", "0.5-SNAPSHOT"), is(true));

		assertThat(AsakusafwConfiguration.containsVersion("0.5.1", "0.5.2", "0.5-SNAPSHOT"), is(false));
		assertThat(AsakusafwConfiguration.containsVersion("0.5.2", "0.5.2", "0.5-SNAPSHOT"), is(true));
		assertThat(AsakusafwConfiguration.containsVersion("0.5.3", "0.5.2", "0.5-SNAPSHOT"), is(true));
		assertThat(AsakusafwConfiguration.containsVersion("0.5-SNAPSHOT", "0.5.2", "0.5-SNAPSHOT"), is(true));

		assertThat(AsakusafwConfiguration.containsVersion("0.4-SNAPSHOT", "0.5.2", "ANY"), is(false));
		assertThat(AsakusafwConfiguration.containsVersion("0.5-SNAPSHOT", "0.5.2", "ANY"), is(true));
	}

	@Test
	public void compareVersion() {
		assertThat(AsakusafwConfiguration.compareVersion("0.5", "0.5"), is(0));
		assertThat(AsakusafwConfiguration.compareVersion("0.5.2", "0.5.2"), is(0));

		assertThat(AsakusafwConfiguration.compareVersion("0.5.0", "0.5.2") < 0, is(true));
		assertThat(AsakusafwConfiguration.compareVersion("0.5.2", "0.5.0") > 0, is(true));

		assertThat(AsakusafwConfiguration.compareVersion("0.5", "0.5.2") < 0, is(true));
		assertThat(AsakusafwConfiguration.compareVersion("0.5.2", "0.5") > 0, is(true));

		assertThat(AsakusafwConfiguration.compareVersion("0.5", "0.5-SNAPSHOT") < 0, is(true));
		assertThat(AsakusafwConfiguration.compareVersion("0.5-SNAPSHOT", "0.5") > 0, is(true));

		assertThat(AsakusafwConfiguration.compareVersion("0.5.2", "0.5-SNAPSHOT") < 0, is(true));
		assertThat(AsakusafwConfiguration.compareVersion("0.5-SNAPSHOT", "0.5.2") > 0, is(true));

		assertThat(AsakusafwConfiguration.compareVersion("0.5", null) < 0, is(true));
		assertThat(AsakusafwConfiguration.compareVersion(null, "0.5") > 0, is(true));
	}
}
