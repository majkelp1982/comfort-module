package pl.smarthouse.comfortmodule.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.experimental.UtilityClass;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRangeMode;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;

@UtilityClass
public class TimeRangeUtils {
  public boolean isWeekend() {
    return List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        .contains(LocalDateTime.now().getDayOfWeek());
  }

  public boolean inTimeRange(final Set<TimeRange> timeRanges) {
    if (Objects.isNull(timeRanges)) {
      return false;
    }
    final LocalTime currentTime = LocalTime.now();
    final Optional<TimeRange> timeRange =
        timeRanges.stream()
            .filter(
                range ->
                    range.getFrom().isBefore(currentTime) && range.getTo().isAfter(currentTime))
            .findAny();
    return timeRange.isPresent();
  }

  public Set<TimeRange> getTimeRangesByDayOfTheWeek(
      TimeRangeMode timeRangeMode,
      final Set<TimeRange> weekendTimeRanges,
      final Set<TimeRange> workdayTimeRanges) {
    if (Objects.isNull(timeRangeMode)) {
      timeRangeMode = TimeRangeMode.AUTO;
    }
    return switch (timeRangeMode) {
      case AUTO -> isWeekend() ? weekendTimeRanges : workdayTimeRanges;
      case FORCE_WORKDAY -> workdayTimeRanges;
      case FORCE_WEEKEND -> weekendTimeRanges;
      case FORCE_OFF -> Set.of();
      case FORCE_ON -> Set.of(
          TimeRange.builder().from(LocalTime.of(0, 0)).to(LocalTime.of(23, 59)).build());
    };
  }
}
