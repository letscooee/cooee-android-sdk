package com.letscooee.models.v3.element;

import android.os.Parcel;
import com.letscooee.models.v3.block.*;

public abstract class BaseChildElement extends BaseElement {

    private int flexGrow;
    private int flexShrink;

    protected BaseChildElement(Parcel in) {
        super(in);

        flexGrow = in.readInt();
        flexShrink = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(flexGrow);
        dest.writeInt(flexShrink);
    }

    public int getFlexGrow() {
        return flexGrow;
    }

    public int getFlexShrink() {
        return flexShrink;
    }
}
