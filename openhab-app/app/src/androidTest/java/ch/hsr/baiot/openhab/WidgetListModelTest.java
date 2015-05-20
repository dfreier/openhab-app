package ch.hsr.baiot.openhab;

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.List;

import ch.hsr.baiot.openhab.sdk.model.Widget;
import ch.hsr.baiot.openhab.sdk.util.ListUtils;

/**
 * Created by dominik on 18.05.15.
 */
public class WidgetListModelTest extends AndroidTestCase {

    public void testListUtils() {
        Widget[] widgets1 = new Widget[4];
        Widget[] widgets2 = new Widget[3];
        widgets1[0] = new Widget();
        widgets1[1] = new Widget();
        widgets1[2] = new Widget();
        widgets1[3] = new Widget();
        widgets2[0] = new Widget();
        widgets2[1] = new Widget();
        widgets2[2] = new Widget();

        widgets1[0].widgetId = "0";
        widgets1[0].label = "label 0.0";
        widgets1[1].widgetId = "1";
        widgets1[2].widgetId = "2";
        widgets1[3].widgetId = "4";

        widgets2[0].widgetId = "4";
        widgets2[1].widgetId = "0";
        widgets2[1].label = "0.1";
        widgets2[2].widgetId = "3";


        assertEquals(widgets1[0], widgets2[1]);

        List<Widget> original = Arrays.asList(widgets1);
        List<Widget> modified = Arrays.asList(widgets2);

        List<Widget> add = ListUtils.added(original, modified);
        List<Widget> remove = ListUtils.removed(original, modified);
        List<Widget> intersect = ListUtils.intersect(original, modified);
        List<Widget> changed = ListUtils.changed(original, modified);
        List<Widget> moved = ListUtils.moved(original, modified);

        assertEquals(1, add.size());
        assertEquals("3", add.get(0).widgetId);

        assertEquals(2, remove.size());
        assertEquals("1", remove.get(0).widgetId);
        assertEquals("2", remove.get(1).widgetId);

        assertEquals(2, intersect.size());
        assertEquals("0", intersect.get(0).widgetId);
        assertEquals("4", intersect.get(1).widgetId);

        assertEquals(1, changed.size());
        assertEquals("0", changed.get(0).widgetId);

        assertEquals(2, moved.size());
        assertEquals("4", moved.get(0).widgetId);
        assertEquals("0", moved.get(1).widgetId);

    }

    public void testHasEqualMembers() {
        Widget w1 = new Widget();
        Widget w2 = new Widget();
        w1.widgetId = "0";
        w2.widgetId = "0";
        w1.label = "label old";
        w2.label = "label old";

        assertTrue(w1.hasEqualMembers(w2));

        w2.label = "label new";
        assertFalse(w1.hasEqualMembers(w2));

    }
}
