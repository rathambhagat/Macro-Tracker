"""
Minimalist Offline Macro Tracker
KivyMD dark theme + SQLite3, offline-only.
"""

import os
import sqlite3
from datetime import date, datetime

from kivy.lang import Builder
from kivy.properties import ListProperty, NumericProperty, StringProperty
from kivy.uix.screenmanager import Screen, ScreenManager
from kivymd.app import MDApp
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.list import MDList, OneLineListItem
from kivymd.uix.snackbar import Snackbar


DB_NAME = "macro_tracker.db"


# --------------------------------------------------------------------------
# Database layer
# --------------------------------------------------------------------------
class Database:
    """Thin wrapper around a local, offline SQLite database."""

    def __init__(self, db_path=None):
        if db_path is None:
            app = MDApp.get_running_app()
            base_dir = app.user_data_dir if app else "."
            db_path = os.path.join(base_dir, DB_NAME)

        db_dir = os.path.dirname(os.path.abspath(db_path))
        if db_dir:
            os.makedirs(db_dir, exist_ok=True)

        self.db_path = db_path
        self.conn = sqlite3.connect(self.db_path)
        self.conn.row_factory = sqlite3.Row
        self._create_tables()

    def _create_tables(self):
        cur = self.conn.cursor()

        cur.execute(
            """
            CREATE TABLE IF NOT EXISTS entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                entry_date TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                kcal REAL NOT NULL DEFAULT 0,
                protein REAL NOT NULL DEFAULT 0,
                carbs REAL NOT NULL DEFAULT 0,
                fats REAL NOT NULL DEFAULT 0
            )
            """
        )

        cur.execute(
            """
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                kcal REAL NOT NULL DEFAULT 1700,
                protein REAL NOT NULL DEFAULT 115,
                carbs REAL NOT NULL DEFAULT 195,
                fats REAL NOT NULL DEFAULT 50
            )
            """
        )

        cur.execute("SELECT COUNT(*) as c FROM goals")
        if cur.fetchone()["c"] == 0:
            cur.execute(
                "INSERT INTO goals (id, kcal, protein, carbs, fats) "
                "VALUES (1, 1700, 115, 195, 50)"
            )

        self.conn.commit()

    # -- Entries -------------------------------------------------------------
    def add_entry(self, kcal, protein, carbs, fats):
        today = date.today().isoformat()
        now = datetime.now().isoformat(timespec="seconds")

        self.conn.execute(
            "INSERT INTO entries (entry_date, timestamp, kcal, protein, carbs, fats) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (today, now, kcal, protein, carbs, fats),
        )
        self.conn.commit()

    def get_today_totals(self):
        today = date.today().isoformat()
        return self._get_totals_for_date(today)

    def _get_totals_for_date(self, day_str):
        cur = self.conn.execute(
            """
            SELECT COALESCE(SUM(kcal), 0) AS kcal,
                   COALESCE(SUM(protein), 0) AS protein,
                   COALESCE(SUM(carbs), 0) AS carbs,
                   COALESCE(SUM(fats), 0) AS fats
            FROM entries
            WHERE entry_date = ?
            """,
            (day_str,),
        )

        row = cur.fetchone()
        return dict(row)

    def get_history(self, limit=60):
        """Return totals grouped by date, most recent first."""
        cur = self.conn.execute(
            """
            SELECT entry_date,
                   COALESCE(SUM(kcal), 0) AS kcal,
                   COALESCE(SUM(protein), 0) AS protein,
                   COALESCE(SUM(carbs), 0) AS carbs,
                   COALESCE(SUM(fats), 0) AS fats
            FROM entries
            GROUP BY entry_date
            ORDER BY entry_date DESC
            LIMIT ?
            """,
            (limit,),
        )

        return [dict(r) for r in cur.fetchall()]

    # -- Goals ---------------------------------------------------------------
    def get_goals(self):
        cur = self.conn.execute(
            "SELECT kcal, protein, carbs, fats FROM goals WHERE id = 1"
        )
        return dict(cur.fetchone())

    def save_goals(self, kcal, protein, carbs, fats):
        self.conn.execute(
            "UPDATE goals SET kcal=?, protein=?, carbs=?, fats=? WHERE id=1",
            (kcal, protein, carbs, fats),
        )
        self.conn.commit()

    def close(self):
        self.conn.close()


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------
def safe_float(text):
    """Convert a text field to float. Blank / invalid input -> 0.0."""
    if text is None:
        return 0.0

    text = str(text).strip()
    if text == "":
        return 0.0

    try:
        value = float(text)
        return max(value, 0.0)
    except ValueError:
        return 0.0


# --------------------------------------------------------------------------
# Reusable widget
# --------------------------------------------------------------------------
class MacroBar(MDBoxLayout):
    label = StringProperty("")
    current = NumericProperty(0)
    target = NumericProperty(100)
    unit = StringProperty("")
    bar_color = ListProperty([0.18, 0.65, 0.61, 1])


# --------------------------------------------------------------------------
# KV layout
# --------------------------------------------------------------------------
KV = """
#:import dp kivy.metrics.dp

<MacroBar>:
    orientation: "vertical"
    adaptive_height: True
    spacing: dp(4)
    padding: [0, dp(6), 0, dp(6)]
    bar_color: app.theme_cls.primary_color

    MDBoxLayout:
        adaptive_height: True

        MDLabel:
            text: root.label
            font_style: "Subtitle1"
            adaptive_height: True

        MDLabel:
            text: str(int(root.current)) + " / " + str(int(root.target)) + " " + root.unit
            halign: "right"
            theme_text_color: "Secondary"
            adaptive_height: True

    MDProgressBar:
        id: bar
        size_hint_y: None
        height: dp(10)
        value: min(root.current, root.target)
        max: root.target if root.target > 0 else 1
        color: root.bar_color


<DashboardScreen>:
    name: "dashboard"

    MDBoxLayout:
        orientation: "vertical"

        MDTopAppBar:
            title: "Macro Tracker"
            elevation: 2
            right_action_items:
                [
                ["history", lambda x: root.go_history()],
                ["cog", lambda x: root.go_settings()],
                ]

        ScrollView:
            MDBoxLayout:
                orientation: "vertical"
                adaptive_height: True
                padding: dp(16)
                spacing: dp(16)

                MDCard:
                    orientation: "vertical"
                    padding: dp(16)
                    spacing: dp(6)
                    adaptive_height: True
                    radius: [dp(16), dp(16), dp(16), dp(16)]
                    elevation: 1

                    MDLabel:
                        text: "Today's Progress"
                        font_style: "H6"
                        adaptive_height: True

                    MacroBar:
                        id: kcal_bar
                        label: "Calories"
                        unit: "kcal"

                    MacroBar:
                        id: protein_bar
                        label: "Protein"
                        unit: "g"

                    MacroBar:
                        id: carbs_bar
                        label: "Carbs"
                        unit: "g"

                    MacroBar:
                        id: fats_bar
                        label: "Fats"
                        unit: "g"

                MDCard:
                    orientation: "vertical"
                    padding: dp(16)
                    spacing: dp(12)
                    adaptive_height: True
                    radius: [dp(16), dp(16), dp(16), dp(16)]
                    elevation: 1

                    MDLabel:
                        text: "Quick Add"
                        font_style: "H6"
                        adaptive_height: True

                    MDTextField:
                        id: input_kcal
                        hint_text: "Kcal"
                        input_filter: "float"
                        mode: "rectangle"

                    MDTextField:
                        id: input_protein
                        hint_text: "Protein (g)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDTextField:
                        id: input_carbs
                        hint_text: "Carbs (g)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDTextField:
                        id: input_fats
                        hint_text: "Fats (g)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDRaisedButton:
                        text: "LOG MACROS"
                        font_style: "Button"
                        size_hint_x: 1
                        size_hint_y: None
                        height: dp(50)
                        md_bg_color: app.theme_cls.primary_color
                        on_release: root.log_macros()


<SettingsScreen>:
    name: "settings"

    MDBoxLayout:
        orientation: "vertical"

        MDTopAppBar:
            title: "Daily Targets"
            elevation: 2
            left_action_items: [["arrow-left", lambda x: root.go_back()]]

        ScrollView:
            MDBoxLayout:
                orientation: "vertical"
                adaptive_height: True
                padding: dp(16)
                spacing: dp(16)

                MDCard:
                    orientation: "vertical"
                    padding: dp(16)
                    spacing: dp(12)
                    adaptive_height: True
                    radius: [dp(16), dp(16), dp(16), dp(16)]
                    elevation: 1

                    MDTextField:
                        id: goal_kcal
                        hint_text: "Daily Calories (kcal)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDTextField:
                        id: goal_protein
                        hint_text: "Daily Protein (g)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDTextField:
                        id: goal_carbs
                        hint_text: "Daily Carbs (g)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDTextField:
                        id: goal_fats
                        hint_text: "Daily Fats (g)"
                        input_filter: "float"
                        mode: "rectangle"

                    MDRaisedButton:
                        text: "SAVE TARGETS"
                        size_hint_x: 1
                        size_hint_y: None
                        height: dp(50)
                        md_bg_color: app.theme_cls.primary_color
                        on_release: root.save_goals()


<HistoryScreen>:
    name: "history"

    MDBoxLayout:
        orientation: "vertical"

        MDTopAppBar:
            title: "History"
            elevation: 2
            left_action_items: [["arrow-left", lambda x: root.go_back()]]

        ScrollView:
            MDList:
                id: history_list
"""


# --------------------------------------------------------------------------
# Screens
# --------------------------------------------------------------------------
class DashboardScreen(Screen):
    def on_pre_enter(self, *args):
        self.refresh()

    def refresh(self):
        app = MDApp.get_running_app()
        if not app or not hasattr(app, "db"):
            return

        if not hasattr(self, "ids") or "kcal_bar" not in self.ids:
            return

        goals = app.db.get_goals()
        totals = app.db.get_today_totals()

        self.ids.kcal_bar.target = goals["kcal"]
        self.ids.kcal_bar.current = totals["kcal"]

        self.ids.protein_bar.target = goals["protein"]
        self.ids.protein_bar.current = totals["protein"]

        self.ids.carbs_bar.target = goals["carbs"]
        self.ids.carbs_bar.current = totals["carbs"]

        self.ids.fats_bar.target = goals["fats"]
        self.ids.fats_bar.current = totals["fats"]

    def log_macros(self):
        kcal = safe_float(self.ids.input_kcal.text)
        protein = safe_float(self.ids.input_protein.text)
        carbs = safe_float(self.ids.input_carbs.text)
        fats = safe_float(self.ids.input_fats.text)

        if kcal == 0 and protein == 0 and carbs == 0 and fats == 0:
            Snackbar(text="Enter at least one value before logging.").open()
            return

        app = MDApp.get_running_app()
        app.db.add_entry(kcal, protein, carbs, fats)

        self.ids.input_kcal.text = ""
        self.ids.input_protein.text = ""
        self.ids.input_carbs.text = ""
        self.ids.input_fats.text = ""

        self.refresh()
        Snackbar(text="Logged!").open()

    def go_settings(self):
        self.manager.current = "settings"

    def go_history(self):
        self.manager.current = "history"


class SettingsScreen(Screen):
    def on_pre_enter(self, *args):
        app = MDApp.get_running_app()
        if not app or not hasattr(app, "db"):
            return

        if not hasattr(self, "ids") or "goal_kcal" not in self.ids:
            return

        goals = app.db.get_goals()

        self.ids.goal_kcal.text = str(int(goals["kcal"]))
        self.ids.goal_protein.text = str(int(goals["protein"]))
        self.ids.goal_carbs.text = str(int(goals["carbs"]))
        self.ids.goal_fats.text = str(int(goals["fats"]))

    def save_goals(self):
        kcal = safe_float(self.ids.goal_kcal.text) or 1
        protein = safe_float(self.ids.goal_protein.text) or 1
        carbs = safe_float(self.ids.goal_carbs.text) or 1
        fats = safe_float(self.ids.goal_fats.text) or 1

        app = MDApp.get_running_app()
        app.db.save_goals(kcal, protein, carbs, fats)

        Snackbar(text="Targets saved.").open()
        self.manager.current = "dashboard"

    def go_back(self):
        self.manager.current = "dashboard"


class HistoryScreen(Screen):
    def on_pre_enter(self, *args):
        self.populate()

    def populate(self):
        app = MDApp.get_running_app()
        if not app or not hasattr(app, "db"):
            return

        if not hasattr(self, "ids") or "history_list" not in self.ids:
            return

        history = app.db.get_history()
        self.ids.history_list.clear_widgets()

        if not history:
            self.ids.history_list.add_widget(
                OneLineListItem(text="No history yet.")
            )
            return

        for day in history:
            summary = (
                f"{day['entry_date']}  —  "
                f"{int(day['kcal'])} kcal | "
                f"P {int(day['protein'])}g | "
                f"C {int(day['carbs'])}g | "
                f"F {int(day['fats'])}g"
            )
            self.ids.history_list.add_widget(
                OneLineListItem(text=summary)
            )

    def go_back(self):
        self.manager.current = "dashboard"


# --------------------------------------------------------------------------
# App
# --------------------------------------------------------------------------
class MacroTrackerApp(MDApp):
    def build(self):
        self.theme_cls.theme_style = "Dark"
        self.theme_cls.primary_palette = "Teal"
        self.theme_cls.accent_palette = "Amber"

        os.makedirs(self.user_data_dir, exist_ok=True)
        self.db = Database(os.path.join(self.user_data_dir, DB_NAME))

        Builder.load_string(KV)

        sm = ScreenManager()
        sm.add_widget(DashboardScreen(name="dashboard"))
        sm.add_widget(SettingsScreen(name="settings"))
        sm.add_widget(HistoryScreen(name="history"))
        return sm

    def on_stop(self):
        if hasattr(self, "db"):
            self.db.close()


if __name__ == "__main__":
    MacroTrackerApp().run()