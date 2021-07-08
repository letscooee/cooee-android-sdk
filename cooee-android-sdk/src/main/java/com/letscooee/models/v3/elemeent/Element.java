package com.letscooee.models.v3.elemeent;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.models.v3.block.Background;
import com.letscooee.models.v3.block.Border;
import com.letscooee.models.v3.block.Color;
import com.letscooee.models.v3.block.Position;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.block.Spacing;
import com.letscooee.models.v3.elemeent.property.Alignment;
import com.letscooee.models.v3.elemeent.property.Font;
import com.letscooee.models.v3.elemeent.property.Transform;

public class Element implements Parcelable {

    protected Element(Parcel in) {
        text = in.readString();
        alignment = in.readParcelable(Alignment.class.getClassLoader());
        color = in.readParcelable(Color.class.getClassLoader());
        size = in.readParcelable(Size.class.getClassLoader());
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        transform = in.readParcelable(Transform.class.getClassLoader());
        url = in.readString();
        position = in.readParcelable(Position.class.getClassLoader());
        flexGrow = in.readInt();
        flexShrink = in.readInt();
        group = in.readParcelable(Group.class.getClassLoader());
        type = ElementType.valueOf(in.readString());
    }

    public static final Creator<Element> CREATOR = new Creator<Element>() {
        @Override
        public Element createFromParcel(Parcel in) {
            return new Element(in);
        }

        @Override
        public Element[] newArray(int size) {
            return new Element[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeParcelable(alignment, flags);
        dest.writeParcelable(color, flags);
        dest.writeParcelable(size, flags);
        dest.writeParcelable(bg, flags);
        dest.writeParcelable(border, flags);
        dest.writeParcelable(spacing, flags);
        dest.writeParcelable(transform, flags);
        dest.writeString(url);
        dest.writeParcelable(position, flags);
        dest.writeInt(flexGrow);
        dest.writeInt(flexShrink);
        dest.writeParcelable(group, flags);
        dest.writeString(type.name());
    }

    public enum ElementType {TEXT, BUTTON, IMAGE, VIDEO, GROUP}

    private ElementType type;
    private String text;
    private Alignment alignment;
    private Font font;
    private Color color;
    private Size size;
    private Background bg;
    private Border border;
    private Spacing spacing;
    private Transform transform;
    private String url;
    private Position position;
    private int flexGrow;
    private int flexShrink;
    private Group group;

    public ElementType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Font getFont() {
        return font;
    }

    public Color getColor() {
        return color;
    }

    public Size getSize() {
        return size;
    }

    public Background getBg() {
        return bg;
    }

    public Border getBorder() {
        return border;
    }

    public Spacing getSpacing() {
        return spacing;
    }

    public Transform getTransform() {
        return transform;
    }

    public String getUrl() {
        return url;
    }

    public Position getPosition() {
        return position;
    }

    public int getFlexGrow() {
        return flexGrow;
    }

    public int getFlexShrink() {
        return flexShrink;
    }

    public Group getGroup() {
        return group;
    }
}
