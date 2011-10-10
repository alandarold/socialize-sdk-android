package com.socialize.ui.test.comment;

import com.google.android.testing.mocking.AndroidMock;
import com.google.android.testing.mocking.UsesMocks;
import com.socialize.ui.comment.CommentListItem;
import com.socialize.ui.test.SocializeUIActivityTest;
import com.socialize.ui.util.Colors;
import com.socialize.util.DeviceUtils;

public class CommentListItemTest extends SocializeUIActivityTest {
	
	@UsesMocks ({
		DeviceUtils.class,
		Colors.class
	})
	public void testInit() {
		DeviceUtils deviceUtils = AndroidMock.createMock(DeviceUtils.class);
		Colors colors = AndroidMock.createMock(Colors.class);
		
		AndroidMock.expect(deviceUtils.getDIP(AndroidMock.anyInt())).andReturn(4).anyTimes();
		AndroidMock.expect(colors.getColor(Colors.BODY)).andReturn(1);
		AndroidMock.expect(colors.getColor(Colors.TITLE)).andReturn(1);
		AndroidMock.expect(colors.getColor(Colors.LIST_ITEM_BG)).andReturn(1);
		
		AndroidMock.replay(deviceUtils);
		AndroidMock.replay(colors);
		
		CommentListItem item = new CommentListItem(getContext());
		item.setDeviceUtils(deviceUtils);
		item.setColors(colors);
		
		item.init();
		
		AndroidMock.verify(deviceUtils);
		AndroidMock.verify(colors);
	}
}
