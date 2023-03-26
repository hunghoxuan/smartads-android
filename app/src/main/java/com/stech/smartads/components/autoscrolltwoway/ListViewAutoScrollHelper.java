package com.stech.smartads.components.autoscrolltwoway;

import android.view.View;
import android.widget.ListView;

/**
 * An implementation of {@link AutoScrollHelper} that knows how to scroll
 * through a {@link ListView}.
 */
public class ListViewAutoScrollHelper extends AutoScrollHelper {
	private final TwoWayView mTarget;

	public ListViewAutoScrollHelper(TwoWayView target) {
		super(target);
		mTarget = target;
	}

	@Override
	public void scrollTargetBy(int deltaX, int deltaY) {
		final TwoWayView target = mTarget;
		final int firstPosition = target.getFirstVisiblePosition();
		if (firstPosition == TwoWayView.INVALID_POSITION) {
			return;
		}
		final View firstView = target.getChildAt(0);
		if (firstView == null) {
			return;
		}
		final int newTop = firstView.getTop() - deltaY;
		target.setSelectionFromOffset(firstPosition, newTop);
	}

	@Override
	public boolean canTargetScrollHorizontally(int direction) {
		// List do not scroll horizontally.
		return false;
	}

	@Override
	public boolean canTargetScrollVertically(int direction) {
		final TwoWayView target = mTarget;
		final int itemCount = target.getCount();
		if (itemCount == 0) {
			return false;
		}
		final int childCount = target.getChildCount();
		final int firstPosition = target.getFirstVisiblePosition();
		final int lastPosition = firstPosition + childCount;
		if (direction > 0) {
			// Are we already showing the entire last item?
			if (lastPosition >= itemCount) {
				final View lastView = target.getChildAt(childCount - 1);
				if (lastView.getBottom() <= target.getHeight()) {
					return false;
				}
			}
		} else if (direction < 0) {
			// Are we already showing the entire first item?
			if (firstPosition <= 0) {
				final View firstView = target.getChildAt(0);
				if (firstView.getTop() >= 0) {
					return false;
				}
			}
		} else {
			// The behavior for direction 0 is undefined and we can return
			// whatever we want.
			return false;
		}
		return true;
	}
}
