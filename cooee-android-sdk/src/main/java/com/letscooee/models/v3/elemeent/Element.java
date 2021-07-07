package com.letscooee.models.v3.elemeent;

import com.letscooee.models.v3.elemeent.property.Alignment;
import com.letscooee.models.v3.elemeent.property.Font;

public class Element {

    public enum ElementType {TEXT, BUTTON, IMAGE, VIDEO}

    private ElementType type;
    private String text;
    private Alignment alignment;
    private Font font;
}
