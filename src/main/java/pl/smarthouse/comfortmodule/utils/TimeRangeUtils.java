package pl.smarthouse.comfortmodule.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRange;

@UtilityClass
public class TimeRangeUtils {
  public boolean isWeekend() {
    return List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        .contains(LocalDateTime.now().getDayOfWeek());
  }

  public boolean inTimeRange(final List<TimeRange> timeRanges) {
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

  public List<TimeRange> getTimeRangeByDayOfTheWeek(
      final List<TimeRange> weekendTimeRanges, final List<TimeRange> workdayTimeranges) {
    return isWeekend() ? weekendTimeRanges : workdayTimeranges;
  }
}
