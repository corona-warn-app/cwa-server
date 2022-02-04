package app.coronawarn.server.services.distribution.dgc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    final Map<Integer, Collection<BusinessRule>> map = BusinessRule.filterAndSort(List.of(v1_0_0, v1_0_5, v1_2_0, v2_0_42, v2_1_1));
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

    final Map<Integer, Collection<BusinessRule>> map = BusinessRule.filterAndSort(List.of(one_v1_0_0, two_v1_0_5, v1_2_0, v2_0_42, v2_1_1));
    assertEquals(2, map.size());
    assertEquals(2, map.get(1).size());
    assertEquals(v2_1_1, map.get(2).iterator().next());
  }

}
