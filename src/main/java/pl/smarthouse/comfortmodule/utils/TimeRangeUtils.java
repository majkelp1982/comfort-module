package pl.smarthouse.comfortmodule.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.experimental.UtilityClass;
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
      final Set<TimeRange> weekendTimeRanges, final Set<TimeRange> workdayTimeRanges) {
    return isWeekend() ? weekendTimeRanges : workdayTimeRanges;
  }
}
