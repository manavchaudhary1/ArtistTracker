package com.manga.artisttracker.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Service
public class EnhancedNozomiFileService {
    public List<Integer> parsePostIds(String nozomiUrl) throws IOException {
        List<Integer> postIds = new ArrayList<>();

        try {
            URL url = new URL(nozomiUrl);
            byte[] data;
            try (InputStream inputStream = url.openStream()) {
                data = inputStream.readAllBytes();
            }

            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.order(ByteOrder.BIG_ENDIAN);

            while (buffer.hasRemaining()) {
                if (buffer.remaining() >= 4) {
                    int postId = buffer.getInt();
                    postIds.add(postId);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse nozomi file: " + nozomiUrl, e);
        }

        return postIds;
    }
}
