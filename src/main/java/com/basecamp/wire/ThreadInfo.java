package com.basecamp.wire;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ThreadInfo implements Comparable<ThreadInfo> {
    private final String name;
    private final Long duration;
    private final Long finished;


    @Override
    public int compareTo(ThreadInfo o) {
        return this.duration<o.duration?-1:
                this.duration>o.duration?1:0;
    }
}
