package app.coronawarn.server.services.distribution.dgc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BusinessRuleTest {

  @Test
  void testGroupByMajor() {
    final BusinessRule v1_0_0 = new BusinessRule();
    v1_0_0.setVersion("1.0.0");

    final BusinessRule v1_0_5 = new BusinessRule();
    v1_0_5.setVersion("1.0.5");
    final BusinessRule v1_2_0 = new BusinessRule();
    v1_2_0.setVersion("1.2.0");
    final BusinessRule v2_0_42 = new BusinessRule();
    v2_0_42.setVersion("2.0.42");
    final BusinessRule v2_1_1 = new BusinessRule();
    v2_1_1.setVersion("2.1.1");

    final Map<Integer, BusinessRule> map = BusinessRule.groupByMajor(List.of(v1_0_0, v1_0_5, v1_2_0, v2_0_42, v2_1_1));
    assertEquals(2, map.size());
    assertEquals(v1_2_0, map.get(1));
    assertEquals(v2_1_1, map.get(2));
  }

  @Test
  void testIsSameMajorVersionButNewer() {
    final BusinessRule r1 = new BusinessRule();
    r1.setVersion("2.1.0");

    final BusinessRule r2 = new BusinessRule();
    r2.setVersion("2.1.0");

    assertTrue(r1.isSameMajorVersionButNewer(r2));

    r2.setVersion("2.1.1");
    assertFalse(r1.isSameMajorVersionButNewer(r2));

    r2.setVersion("1.1.0");
    assertFalse(r1.isSameMajorVersionButNewer(r2));

    r2.setVersion("2.0.0");
    assertTrue(r1.isSameMajorVersionButNewer(r2));
  }

  @Test
  void testFilterAndSortWithSameIdentifier() {
    final BusinessRule v1_0_0 = new BusinessRule();
    v1_0_0.setVersion("1.0.0");
    v1_0_0.setIdentifier("one");

    final BusinessRule v1_0_5 = new BusinessRule();
    v1_0_5.setVersion("1.0.5");
    v1_0_5.setIdentifier("one");

    final BusinessRule v1_2_0 = new BusinessRule();
    v1_2_0.setVersion("1.2.0");
    v1_2_0.setIdentifier("one");

    final BusinessRule v2_0_42 = new BusinessRule();
    v2_0_42.setVersion("2.0.42");
    v2_0_42.setIdentifier("one");

    final BusinessRule v2_1_1 = new BusinessRule();
    v2_1_1.setVersion("2.1.1");
    v2_1_1.setIdentifier("one");

    final Map<Integer, Collection<BusinessRule>> map = BusinessRule
        .filterAndSort(List.of(v1_0_0, v1_0_5, v1_2_0, v2_0_42, v2_1_1));
    assertEquals(2, map.size());
    assertEquals(v1_2_0, map.get(1).iterator().next());
    assertEquals(v2_1_1, map.get(2).iterator().next());
  }

  @Test
  void testFilterAndSort() {
    final BusinessRule one_v1_0_0 = new BusinessRule();
    one_v1_0_0.setVersion("1.0.0");
    one_v1_0_0.setIdentifier("one");

    final BusinessRule two_v1_0_5 = new BusinessRule();
    two_v1_0_5.setVersion("1.0.5");
    two_v1_0_5.setIdentifier("two");

    final BusinessRule v1_2_0 = new BusinessRule();
    v1_2_0.setVersion("1.2.0");
    v1_2_0.setIdentifier("one");

    final BusinessRule v2_0_42 = new BusinessRule();
    v2_0_42.setVersion("2.0.42");
    v2_0_42.setIdentifier("one");

    final BusinessRule v2_1_1 = new BusinessRule();
    v2_1_1.setVersion("2.1.1");
    v2_1_1.setIdentifier("one");

    final Map<Integer, Collection<BusinessRule>> map = BusinessRule
        .filterAndSort(List.of(one_v1_0_0, two_v1_0_5, v1_2_0, v2_0_42, v2_1_1));
    assertEquals(2, map.size());
    assertEquals(2, map.get(1).size());
    assertEquals(v2_1_1, map.get(2).iterator().next());
  }

  /**
   * Example: 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 <
   * 1.0.0.
   */
  @Test
  void testVersionParsing() {
    String[] strings = new String[] { "1.0.0-rc.6", "1.0.0-rc.60", "1.0.0-RC.30", "1.0.0-RC.3" };
    Semver[] versions = new Semver[strings.length];

    for (int i = 0; i < strings.length; i++) {
      BusinessRule b = new BusinessRule();
      b.setVersion(strings[i]);
      versions[i] = b.version();
    }

    Arrays.sort(versions);
    Semver smallest = new Semver("1.0.0-rc.0", SemverType.LOOSE);
    Semver largest = new Semver("1.0.0", SemverType.LOOSE);
    for (int i = 0; i < versions.length; i++) {
      assertTrue(smallest.isLowerThan(versions[i]));
      assertTrue(largest.isGreaterThan(versions[i]));
    }
  }
}
