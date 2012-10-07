package com.higheranimals.measurewords;

import android.os.Parcel;
import android.os.Parcelable;

final public class Answer implements Parcelable {
    private final String hanzi;
    private final String pinyin;
    private final String english;

    public Answer(String hanzi, String pinyin, String english) {
        this.hanzi = hanzi;
        this.pinyin = pinyin;
        this.english = english;
    }

    public String getHanzi() {
        return hanzi;
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getEnglish() {
        return english;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Answer))
            return false;
        Answer a = (Answer) o;
        return hanzi.equals(a.getHanzi()) && pinyin.equals(a.getPinyin())
                && english.equals(a.getEnglish());
    }

    public static final Parcelable.Creator<Answer> CREATOR = new Parcelable.Creator<Answer>() {

        @Override
        public Answer createFromParcel(Parcel source) {
            return new Answer(source);
        }

        @Override
        public Answer[] newArray(int size) {
            return new Answer[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hanzi);
        dest.writeString(pinyin);
        dest.writeString(english);
    }

    private Answer(Parcel source) {
        hanzi = source.readString();
        pinyin = source.readString();
        english = source.readString();
    }
}
