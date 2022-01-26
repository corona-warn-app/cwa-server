package app.coronawarn.server.services.distribution.dgc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class BusinessRuleTest {

  @Test
  void testFilterAndSortBusinessRuleArray() {
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

    final Map<Integer, BusinessRule> map = BusinessRule.filterAndSort(v1_0_0, v1_0_5, v1_2_0, v2_0_42, v2_1_1);
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
}
