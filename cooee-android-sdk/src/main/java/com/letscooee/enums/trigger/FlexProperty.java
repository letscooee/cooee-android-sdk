package com.letscooee.enums.trigger;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;

/**
 * Define all flex property at one place
 *
 * @author Ashish Gaikwad 06/08/21
 * @since 1.0.0
 */

public class FlexProperty {

    public enum Direction {
        ROW(FlexDirection.ROW),
        COLUMN(FlexDirection.COLUMN),
        ROW_REVERSE(FlexDirection.ROW_REVERSE),
        COLUMN_REVERSE(FlexDirection.COLUMN_REVERSE);

        private final int value;

        Direction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Wrap {
        WRAP(FlexWrap.WRAP),
        NOWRAP(FlexWrap.NOWRAP),
        WRAP_REVERSE(FlexWrap.WRAP_REVERSE);

        private final int value;

        Wrap(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum JustifyContent {
        FLEX_START(com.google.android.flexbox.JustifyContent.FLEX_START),
        FLEX_END(com.google.android.flexbox.JustifyContent.FLEX_END),
        CENTER(com.google.android.flexbox.JustifyContent.CENTER),
        SPACE_BETWEEN(com.google.android.flexbox.JustifyContent.SPACE_BETWEEN),
        SPACE_AROUND(com.google.android.flexbox.JustifyContent.SPACE_AROUND),
        SPACE_EVENLY(com.google.android.flexbox.JustifyContent.SPACE_EVENLY);

        private final int value;

        JustifyContent(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum AlignItems {
        FLEX_START(com.google.android.flexbox.AlignItems.FLEX_START),
        FLEX_END(com.google.android.flexbox.AlignItems.FLEX_END),
        CENTER(com.google.android.flexbox.AlignItems.CENTER),
        BASELINE(com.google.android.flexbox.AlignItems.BASELINE),
        STRETCH(com.google.android.flexbox.AlignItems.STRETCH);

        private final int value;

        AlignItems(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum AlignContent {
        FLEX_START(com.google.android.flexbox.AlignContent.FLEX_START),
        FLEX_END(com.google.android.flexbox.AlignContent.FLEX_END),
        CENTER(com.google.android.flexbox.AlignContent.CENTER),
        SPACE_BETWEEN(com.google.android.flexbox.AlignContent.SPACE_BETWEEN),
        SPACE_AROUND(com.google.android.flexbox.AlignContent.SPACE_AROUND),
        STRETCH(com.google.android.flexbox.AlignContent.STRETCH);

        private final int value;

        AlignContent(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
