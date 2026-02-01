package com.yasirakbal.secureloanapi.common.mapper;

public interface BaseMapper<Source, Target> {
    Source map(Target target);
}
