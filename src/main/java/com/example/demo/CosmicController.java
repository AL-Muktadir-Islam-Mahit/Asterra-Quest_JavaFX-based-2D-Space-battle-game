package com.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CosmicController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button resetBtn;
    @FXML private FlowPane chipsPane;
    @FXML private HBox statsBox;
    @FXML private TilePane gridPane;
    @FXML private Button backBtn;

    private final List<Event> events = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final ZoneId userZone = ZoneId.systemDefault();
    private final DateTimeFormatter fmt = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(userZone);

    @FXML
    public void initialize() {
        loadData();
        setupMonthCombo();
        setupSortCombo();
        createChips();
        attachListeners();
        Platform.runLater(() -> {
            adjustColumns(gridPane.getWidth());
            render();
        });
    }

    @FXML
    private void handleBack(){
        Main.switchScene("home.fxml");
    }

    private void loadData() {
        events.clear();

        events.add(new Event("perseid-2025","Perseid Meteor Shower","Meteor Shower",
                Instant.parse("2025-08-11T18:00:00Z"), Instant.parse("2025-08-12T18:00:00Z"),
                Instant.parse("2025-08-12T01:00:00Z"),
                "Best in Northern Hemisphere; some moonlight interference", true,
                "Annual shower from Comet Swift–Tuttle. Fast, bright meteors; potential fireballs.",
                "https://www.timeanddate.com/astronomy/meteor-shower/perseid.html"));

        events.add(new Event("planet-parade-aug-2025","Six-Planet Parade","Planetary Alignment",
                Instant.parse("2025-08-17T00:00:00Z"), Instant.parse("2025-08-20T23:59:59Z"),
                Instant.parse("2025-08-19T09:00:00Z"),
                "Dawn sky; low horizons help. Use app/sky map for placement.", true,
                "A rare lineup of six planets graces the morning sky. Best seen before sunrise.",
                "https://www.livescience.com/space/planets/planetary-alignment"));

        events.add(new Event("total-lunar-eclipse-sep-2025","Total Lunar Eclipse (Blood Moon)","Eclipse",
                Instant.parse("2025-09-07T16:00:00Z"), Instant.parse("2025-09-08T02:00:00Z"),
                Instant.parse("2025-09-07T20:55:00Z"),
                "Americas, Europe, Africa; partial phases wider", true,
                "The Moon passes fully into Earth’s shadow, turning deep red for ~1 hour at totality.",
                "https://eclipse.gsfc.nasa.gov/LEplot/LEplot2001/LE2025Sep07T.pdf"));

        events.add(new Event("partial-solar-eclipse-sep-2025","Partial Solar Eclipse","Eclipse",
                Instant.parse("2025-09-21T13:00:00Z"), Instant.parse("2025-09-21T18:00:00Z"),
                Instant.parse("2025-09-21T15:30:00Z"),
                "Parts of South America, Antarctica, South Atlantic", false,
                "The Moon covers a portion of the Sun. Use proper solar filters — never look at the Sun without certified protection.",
                "https://eclipse.gsfc.nasa.gov/SEplot/SEplot2001/SE2025Sep21P.pdf"));

        events.add(new Event("orionids-2025","Orionid Meteor Shower","Meteor Shower",
                Instant.parse("2025-10-20T00:00:00Z"), Instant.parse("2025-10-21T23:59:59Z"),
                Instant.parse("2025-10-21T05:00:00Z"),
                "Both hemispheres; best after midnight", true,
                "Debris from Halley’s Comet. Swift meteors; normally 10–20 per hour at peak in dark skies.",
                "https://www.timeanddate.com/astronomy/meteor-shower/orionid.html"));

        events.add(new Event("leonids-2025","Leonid Meteor Shower","Meteor Shower",
                Instant.parse("2025-11-17T00:00:00Z"), Instant.parse("2025-11-18T23:59:59Z"),
                Instant.parse("2025-11-18T04:00:00Z"),
                "Best after midnight; Northern Hemisphere favored", true,
                "Famous for occasional storms; typical years bring 10–15 meteors/hour under dark skies.",
                "https://www.imo.net/resources/calendar/"));

        events.add(new Event("geminids-2025","Geminid Meteor Shower","Meteor Shower",
                Instant.parse("2025-12-13T00:00:00Z"), Instant.parse("2025-12-14T23:59:59Z"),
                Instant.parse("2025-12-14T02:00:00Z"),
                "Both hemispheres; prolific and bright meteors", true,
                "One of the year’s best showers; slow, colorful meteors produced by asteroid 3200 Phaethon.",
                "https://www.timeanddate.com/astronomy/meteor-shower/geminid.html"));

        events.add(new Event("t-crb-watch","T Coronae Borealis Recurrent Nova (Watchlist)","Nova",
                Instant.parse("2025-03-01T00:00:00Z"), Instant.parse("2026-08-31T23:59:59Z"),
                null,
                "Northern Hemisphere; brief naked-eye window when it erupts", true,
                "A white dwarf + red giant system expected to undergo a nova outburst this cycle. Timing uncertain — could brighten to mag ~2 for a few days.",
                "https://science.nasa.gov/universe/stars/t-coronae-borealis/"));

        events.add(new Event("sn-2025oq","Supernova SN 2025oq (NGC 2744)","Supernova",
                Instant.parse("2025-05-15T00:00:00Z"), Instant.parse("2025-12-31T23:59:59Z"),
                null,
                "Telescope imaging; not visible to naked eye", false,
                "Ongoing follow-up observations of an external-galaxy supernova. Great target for remote scopes and image processing practice.",
                "https://www.virtualtelescope.eu/"));

        events.add(new Event("sn-2025mvn","Supernova SN 2025mvn (NGC 5033)","Supernova",
                Instant.parse("2025-06-20T00:00:00Z"), Instant.parse("2025-12-31T23:59:59Z"),
                null,
                "Telescope imaging; not visible to naked eye", false,
                "A discovered supernova in the spiral galaxy NGC 5033. Ideal for amateur spectroscopy with sufficient aperture.",
                "https://www.virtualtelescope.eu/"));

        events.add(new Event("beaver-supermoon-2025","Full Beaver Moon (Supermoon)","Lunar",
                Instant.parse("2025-11-05T00:00:00Z"), Instant.parse("2025-11-05T23:59:59Z"),
                Instant.parse("2025-11-05T11:19:00Z"),
                "Global (weather permitting)", true,
                "The largest-appearing full Moon of 2025 — great for low-horizon photos at moonrise.",
                "https://www.timeanddate.com/moon/phases/"));

        events.add(new Event("cold-supermoon-2025","Cold Moon (Supermoon)","Lunar",
                Instant.parse("2025-12-04T00:00:00Z"), Instant.parse("2025-12-04T23:59:59Z"),
                Instant.parse("2025-12-04T17:00:00Z"),
                "Global (weather permitting)", true,
                "A bright winter full Moon appearing larger due to near-perigee timing.",
                "https://www.timeanddate.com/moon/phases/"));

        // build categories
        categories.clear();
        categories.addAll(events.stream().map(e -> e.category).distinct().collect(Collectors.toList()));
    }

    private void setupMonthCombo() {
        monthCombo.getItems().clear();
        monthCombo.getItems().add("Any month");
        for (int i = 1; i <= 12; i++) {
            Month m = Month.of(i);
            monthCombo.getItems().add(m.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()));
        }
        monthCombo.getSelectionModel().select(0);
    }

    private void setupSortCombo() {
        sortCombo.getItems().setAll("Soonest first", "Latest first", "Name A→Z");
        sortCombo.getSelectionModel().select(0);
    }

    private void createChips() {
        chipsPane.getChildren().clear();
        for (String cat : categories) {
            ToggleButton tb = new ToggleButton(cat);
            tb.getStyleClass().add("chip");
            tb.setUserData(cat);
            tb.setOnAction(e -> render());
            chipsPane.getChildren().add(tb);
        }
    }

    private void attachListeners() {
        searchField.textProperty().addListener((obs, o, n) -> render());
        monthCombo.valueProperty().addListener((o, oldV, newV) -> render());
        sortCombo.valueProperty().addListener((o, oldV, newV) -> render());
        resetBtn.setOnAction(e -> {
            searchField.clear();
            monthCombo.getSelectionModel().select(0);
            sortCombo.getSelectionModel().select(0);
            chipsPane.getChildren().stream().filter(n->n instanceof ToggleButton)
                    .map(n->(ToggleButton)n).forEach(t->t.setSelected(false));
            render();
        });

        gridPane.widthProperty().addListener((obs, oldW, newW) -> adjustColumns(newW.doubleValue()));
    }

    private void adjustColumns(double w) {
        int cols = 3;
        if (w < 640) cols = 1;
        else if (w < 980) cols = 2;
        else cols = 3;
        gridPane.setPrefColumns(cols);
    }

    private void render() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        int monthIndex = monthCombo.getSelectionModel().getSelectedIndex(); // 0 == Any month
        List<String> activeCats = chipsPane.getChildren().stream()
                .filter(n -> n instanceof ToggleButton)
                .map(n -> (ToggleButton) n)
                .filter(ToggleButton::isSelected)
                .map(tb -> (String) tb.getUserData())
                .collect(Collectors.toList());

        List<Event> list = new ArrayList<>(events);

        if (!q.isEmpty()) {
            list = list.stream().filter(e -> (e.title + e.description + e.visibility + e.category)
                    .toLowerCase().contains(q)).collect(Collectors.toList());
        }
        if (monthIndex > 0) {
            int targetMonth = monthIndex; // because we inserted "Any month" at index 0
            list = list.stream().filter(e -> {
                int m = ZonedDateTime.ofInstant(e.start, ZoneOffset.UTC).getMonthValue();
                return m == targetMonth;
            }).collect(Collectors.toList());
        }
        if (!activeCats.isEmpty()) {
            list = list.stream().filter(e -> activeCats.contains(e.category)).collect(Collectors.toList());
        }
        String sort = sortCombo.getValue();
        if ("Soonest first".equals(sort)) {
            list.sort(Comparator.comparing(e -> e.start));
        } else if ("Latest first".equals(sort)) {
            list.sort(Comparator.comparing((Event e) -> e.start).reversed());
        } else {
            list.sort(Comparator.comparing(e -> e.title));
        }

        statsBox.getChildren().clear();
        long upcoming = list.stream().filter(e -> {
            Instant endOrStart = e.end != null ? e.end : e.start;
            return endOrStart.isAfter(Instant.now());
        }).count();

        Label stat1 = new Label("Showing " + list.size() + " events"); stat1.getStyleClass().add("stat");
        Label stat2 = new Label("Upcoming from now: " + upcoming); stat2.getStyleClass().add("stat");
        HBox catBox = new HBox(6);
        catBox.getStyleClass().add("stat");
        Label catLabel = new Label("Categories: ");
        catBox.getChildren().add(catLabel);
        categories.forEach(c -> {
            Label t = new Label(c);
            t.getStyleClass().add("tag");
            catBox.getChildren().add(t);
        });
        statsBox.getChildren().addAll(stat1, stat2, catBox);

        gridPane.getChildren().clear();
        if (list.isEmpty()) {
            gridPane.getChildren().add(makeEmptyCard());
        } else {
            list.forEach(e -> gridPane.getChildren().add(makeCard(e)));
        }
    }

    private Region makeEmptyCard() {
        VBox box = new VBox(6);
        box.getStyleClass().add("card");
        Label h = new Label("No matches"); h.getStyleClass().add("title2");
        Label d = new Label("Try clearing filters, or search a different term."); d.getStyleClass().add("desc");
        d.setWrapText(true);
        box.getChildren().addAll(h,d);
        box.setPrefWidth(320);
        return box;
    }

    private Region makeCard(Event e) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(320);

        Label badge = new Label(badgeFor(e));
        badge.getStyleClass().add("badge");

        Instant now = Instant.now();
        String soon = e.start.isAfter(now) ? "Upcoming" : "Ongoing / Past";
        HBox when = new HBox(6);
        when.getStyleClass().add("when");
        Region dot = new Region();
        dot.getStyleClass().add("dot");
        Label whenLabel = new Label(soon + " • " + fmt.format(e.start));
        when.getChildren().addAll(dot, whenLabel);

        Label title = new Label(e.title); title.getStyleClass().add("title2");
        Label desc = new Label(e.description); desc.setWrapText(true); desc.getStyleClass().add("desc");

        HBox meta = new HBox(6); meta.getStyleClass().add("meta");
        Label vis = new Label("Visibility: " + e.visibility); vis.getStyleClass().add("tag");
        Label eye = new Label(e.nakedEye ? "👁️‍🗨️ Naked-eye" : "🔭 Telescope"); eye.getStyleClass().add("tag");
        meta.getChildren().addAll(vis, eye);
        if (e.peak != null) {
            Label peak = new Label("Peak: " + fmt.format(e.peak)); peak.getStyleClass().add("tag");
            meta.getChildren().add(peak);
        }
        Label cat = new Label("Category: " + e.category); cat.getStyleClass().add("tag");
        meta.getChildren().add(cat);

        HBox cta = new HBox(8); cta.getStyleClass().add("cta");
        Button share = new Button("Share");
        share.getStyleClass().add("btn");
        share.setOnAction(ev -> copyShare(e));

        Button addCal = new Button("Add to calendar");
        addCal.getStyleClass().add("btn");
        addCal.setOnAction(ev -> addToCalendar(e, gridPane.getScene().getWindow()));

        Hyperlink learn = new Hyperlink("Learn more ↗");
        learn.getStyleClass().addAll("btn", "primary");
        learn.setOnAction(ev -> {
            try {
                String url = e.source;
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        cta.getChildren().addAll(share, addCal, learn);
        VBox.setVgrow(cta, Priority.ALWAYS);

        card.getChildren().addAll(badge, when, title, desc, meta, cta);
        return card;
    }

    private String badgeFor(Event e) {
        Map<String, String> map = new HashMap<>();
        map.put("Meteor Shower", "☄️ Meteor");
        map.put("Eclipse", "🌘 Eclipse");
        map.put("Planetary Alignment", "🪐 Alignment");
        map.put("Nova", "✨ Nova");
        map.put("Supernova", "💥 Supernova");
        map.put("Lunar", "🌕 Lunar");
        return map.getOrDefault(e.category, "✦ Event");
    }

    private void addToCalendar(Event e, Window owner) {
        try {
            String dt = isoForIcs(e.start);
            String dtEnd = isoForIcs(e.end != null ? e.end : e.start);
            String ics = "BEGIN:VCALENDAR\n" +
                    "VERSION:2.0\n" +
                    "PRODID:-//Cosmic Watch//EN\n" +
                    "CALSCALE:GREGORIAN\n" +
                    "METHOD:PUBLISH\n" +
                    "BEGIN:VEVENT\n" +
                    "UID:" + e.id + "@cosmicwatch\n" +
                    "DTSTAMP:" + isoForIcs(Instant.now()) + "\n" +
                    "DTSTART:" + dt + "\n" +
                    "DTEND:" + dtEnd + "\n" +
                    "SUMMARY:" + escapeIcs(e.title) + "\n" +
                    "DESCRIPTION:" + escapeIcs(e.description.replace("\n", " ")) + "\n" +
                    "URL:" + e.source + "\n" +
                    "END:VEVENT\n" +
                    "END:VCALENDAR";

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save .ics file");
            chooser.setInitialFileName(e.id + ".ics");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("iCalendar", "*.ics"));
            File file = chooser.showSaveDialog(owner);
            if (file != null) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                    bw.write(ics);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Could not create calendar file: " + ex.getMessage());
        }
    }

    private String isoForIcs(Instant i) {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(i);
    }

    private String escapeIcs(String s) {
        return s.replace("\n", "\\n").replace(",", "\\,");
    }

    private void copyShare(Event e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.title).append(" — ").append(e.category).append("\n");
        sb.append("When: ").append(fmt.format(e.start));
        if (e.end != null) sb.append(" to ").append(fmt.format(e.end));
        sb.append("\n");
        if (e.peak != null) sb.append("Peak: ").append(fmt.format(e.peak)).append("\n");
        sb.append("Visibility: ").append(e.visibility).append("\n");
        sb.append("More: ").append(e.source);

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        boolean ok = clipboard.setContent(content);
        if (ok) {
            showAlert("Copied", "Copied event details to clipboard!");
        } else {
            TextInputDialog dlg = new TextInputDialog(sb.toString());
            dlg.setHeaderText("Copy event details:");
            dlg.showAndWait();
        }
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.show();
    }

    // Simple Event holder (inner class to keep one file)
    private static class Event {
        final String id, title, category, visibility, description, source;
        final Instant start, end, peak;
        final boolean nakedEye;
        Event(String id, String title, String category, Instant start, Instant end, Instant peak,
              String visibility, boolean nakedEye, String description, String source) {
            this.id = id; this.title = title; this.category = category; this.start = start; this.end = end; this.peak = peak;
            this.visibility = visibility; this.nakedEye = nakedEye; this.description = description; this.source = source;
        }
    }
}
