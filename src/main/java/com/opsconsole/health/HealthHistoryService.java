package com.opsconsole.health;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class HealthHistoryService {

    private static final Duration WINDOW = Duration.ofHours(24);
    private static final int MAX_SNAPSHOTS = 500;
    private static final int MAX_CHART_POINTS = 48;

    private final Deque<HealthSnapshot> snapshots = new ConcurrentLinkedDeque<>();

    public void record(int total, int up, Instant at) {
        snapshots.addLast(new HealthSnapshot(at, total, up));
        trim();
    }

    public ServicesUpChart chartLast24Hours(int currentTotal, int currentUp) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(WINDOW);
        List<HealthSnapshot> inWindow = snapshots.stream()
                .filter(s -> !s.at().isBefore(windowStart))
                .toList();

        List<HealthSnapshot> series = inWindow.isEmpty()
                ? List.of(new HealthSnapshot(now, currentTotal, currentUp))
                : downsample(inWindow, MAX_CHART_POINTS);

        int maxY = Math.max(1, series.stream().mapToInt(HealthSnapshot::total).max().orElse(currentTotal));
        if (currentTotal > maxY) {
            maxY = currentTotal;
        }

        List<ChartPoint> points = buildNormalizedPoints(series, windowStart, now, maxY);
        String linePath = buildLinePath(points);
        String areaPath = buildAreaPath(points);
        List<String> xLabels = xAxisLabels(windowStart, now);

        return new ServicesUpChart(
                points,
                maxY,
                currentUp,
                currentTotal,
                linePath,
                areaPath,
                xLabels
        );
    }

    private void trim() {
        Instant cutoff = Instant.now().minus(WINDOW);
        while (!snapshots.isEmpty() && snapshots.peekFirst().at().isBefore(cutoff)) {
            snapshots.pollFirst();
        }
        while (snapshots.size() > MAX_SNAPSHOTS) {
            snapshots.pollFirst();
        }
    }

    private static List<HealthSnapshot> downsample(List<HealthSnapshot> source, int maxPoints) {
        if (source.size() <= maxPoints) {
            return source;
        }
        List<HealthSnapshot> result = new ArrayList<>(maxPoints);
        double step = (double) (source.size() - 1) / (maxPoints - 1);
        for (int i = 0; i < maxPoints; i++) {
            int index = (int) Math.round(i * step);
            result.add(source.get(Math.min(index, source.size() - 1)));
        }
        return result;
    }

    private static List<ChartPoint> buildNormalizedPoints(
            List<HealthSnapshot> series,
            Instant windowStart,
            Instant windowEnd,
            int maxY
    ) {
        long spanMs = Math.max(1, Duration.between(windowStart, windowEnd).toMillis());
        List<ChartPoint> points = new ArrayList<>(series.size());
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

        for (HealthSnapshot snapshot : series) {
            long offsetMs = Duration.between(windowStart, snapshot.at()).toMillis();
            double x = (offsetMs * 100.0) / spanMs;
            double y = 100.0 - ((snapshot.up() * 100.0) / maxY);
            points.add(new ChartPoint(
                    clamp(x, 0, 100),
                    clamp(y, 0, 100),
                    snapshot.up(),
                    labelFmt.format(snapshot.at())
            ));
        }
        return points;
    }

    private static String buildLinePath(List<ChartPoint> points) {
        if (points.isEmpty()) {
            return "M0 100 L100 100";
        }
        StringBuilder sb = new StringBuilder();
        ChartPoint first = points.getFirst();
        sb.append(String.format("M %.2f %.2f", first.x(), first.y()));
        for (int i = 1; i < points.size(); i++) {
            ChartPoint p = points.get(i);
            sb.append(String.format(" L %.2f %.2f", p.x(), p.y()));
        }
        return sb.toString();
    }

    private static String buildAreaPath(List<ChartPoint> points) {
        if (points.isEmpty()) {
            return "M0 100 L100 100 Z";
        }
        StringBuilder sb = new StringBuilder();
        ChartPoint first = points.getFirst();
        sb.append(String.format("M %.2f 100 L %.2f %.2f", first.x(), first.x(), first.y()));
        for (int i = 1; i < points.size(); i++) {
            ChartPoint p = points.get(i);
            sb.append(String.format(" L %.2f %.2f", p.x(), p.y()));
        }
        ChartPoint last = points.getLast();
        sb.append(String.format(" L %.2f 100 Z", last.x()));
        return sb.toString();
    }

    private static List<String> xAxisLabels(Instant start, Instant end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
        long spanMs = Duration.between(start, end).toMillis();
        List<String> labels = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Instant t = start.plusMillis((spanMs * i) / 4);
            labels.add(fmt.format(t));
        }
        return labels;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record HealthSnapshot(Instant at, int total, int up) {
    }

    public record ChartPoint(double x, double y, int upCount, String timeLabel) {
    }

    public record ServicesUpChart(
            List<ChartPoint> points,
            int maxY,
            int currentUp,
            int totalServices,
            String linePath,
            String areaPath,
            List<String> xAxisLabels
    ) {
    }
}
