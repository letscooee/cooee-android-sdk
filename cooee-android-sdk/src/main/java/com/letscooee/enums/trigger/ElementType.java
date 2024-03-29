package com.letscooee.enums.trigger;

import com.letscooee.models.trigger.elements.*;

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
    SHAPE(ShapeElement.class);

    public final Class<? extends BaseElement> elementClass;

    ElementType(Class<? extends BaseElement> clazz) {
        elementClass = clazz;
    }
}
