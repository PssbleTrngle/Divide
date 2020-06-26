package com.possible_triangle.divide.data;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class ModSerializers {

    public static final IDataSerializer<Optional<String>> OPTIONAL_STRING = new IDataSerializer<Optional<String>>() {
        public void write(PacketBuffer buf, Optional<String> value) {
            buf.writeBoolean(value.isPresent());
            value.ifPresent(buf::writeString);
        }

        public Optional<String> read(PacketBuffer buf) {
            return !buf.readBoolean() ? Optional.empty() : Optional.of(buf.readString());
        }

        public Optional<String> copyValue(Optional<String> value) {
            return value;
        }
    };

    static {
        DataSerializers.registerSerializer(OPTIONAL_STRING);
    }

}
