package com.letscooee.enums;

import com.letscooee.models.v3.element.*;

/**
 * Types of elements possible in a engagement trigger.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public enum ElementType {

    TEXT(TextElement.class),
    BUTTON(ButtonElement.class),
    IMAGE(ImageElement.class),
    VIDEO(VideoElement.class),
    GROUP(GroupElement.class);

    public final Class<? extends BaseChildElement> elementClass;

    ElementType(Class<? extends BaseChildElement> clazz) {
        elementClass = clazz;
    }
}
