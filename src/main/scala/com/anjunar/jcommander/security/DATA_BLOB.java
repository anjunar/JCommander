package com.anjunar.jcommander.security;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class DATA_BLOB extends Structure {

    public int cbData;
    public Pointer pbData;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("cbData", "pbData");
    }

    public static class ByReference extends DATA_BLOB implements Structure.ByReference {}
    public static class ByValue extends DATA_BLOB implements Structure.ByValue {}
}
