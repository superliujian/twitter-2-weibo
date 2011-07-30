package h2weibo.utils.filters;

import h2weibo.model.DBHelper;
import junit.framework.TestCase;

/**
 * Test case for UserMappingFilter
 */
public class UserMappingFilterTest extends TestCase {
    public void setUp() throws Exception {
        DBHelper helper = new DBHelper();
        helper.setWeiboId("Xuzhe", "xu_zhe");
        helper.setWeiboId("EatDami", "大米大仙");
    }

    public void testFilter() throws Exception {
        String input = "@xuzhe Let's eat dinner with @eatdami tonight. /cc @xu_lele.";
        UserMappingFilter filter = new UserMappingFilter();
        String s = filter.filter(input);
        assertEquals(s, "@xu_zhe Let's eat dinner with @大米大仙 tonight. /cc @xu_lele.");
    }
}
