package com.lostsidewalk.buffy.engine.audit;

import com.lostsidewalk.buffy.DataAccessException;
import com.lostsidewalk.buffy.DataUpdateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.lang.System.arraycopy;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j(topic = "engineErrorLog")
@Service
public class ErrorLogService {

    public static void logDataAccessException(Date timestamp, DataAccessException e) {
        auditError("data-access-exception", "message={}", timestamp, e.getMessage());
    }

    public static void logDataUpdateException(Date timestamp, DataUpdateException e) {
        auditError("data-not-found-exception", "message={}", timestamp, e.getMessage());
    }

    //

    @SuppressWarnings("SameParameterValue")
    private static void auditError(String logTag, String formatStr, Date timestamp, Object... args) {
        String fullFormatStr = "eventType={}, timestamp={}";
        if (isNotEmpty(formatStr)) {
            fullFormatStr += (", " + formatStr);
        }
        Object[] allArgs = new Object[args.length + 2];
        allArgs[0] = logTag;
        allArgs[1] = timestamp;
        arraycopy(args, 0, allArgs, 2, args.length);
        log.error(fullFormatStr, allArgs);
    }
}
