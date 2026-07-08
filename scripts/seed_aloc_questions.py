#!/usr/bin/env python3
"""
One-time ALOC seeder for the AcademicAI MySQL database.

What it does:
- Fetches questions from ALOC for subject/year/type combinations
- Maps each question into the existing `past_questions` table
- Skips duplicate question text
- Best-effort assigns a topic using subject/topic heuristics

Environment variables:
  ALOC_TOKEN           required
  DB_HOST              default: localhost
  DB_PORT              default: 3306
  DB_NAME              required
  DB_USER              required
  DB_PASSWORD          required
  ALOC_BASE_URL        default: https://questions.aloc.com.ng/api/v2
  ALOC_DELAY_SECONDS    default: 1.0
  ALOC_LIMIT           default: 120

Install:
  pip install requests mysql-connector-python

Run:
  python scripts/seed_aloc_questions.py
"""

from __future__ import annotations

import json
import os
import re
import sys
import time
from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Tuple

import mysql.connector
import requests


SUBJECTS = [
    "english language",
    "mathematics",
    "biology",
    "chemistry",
    "physics",
    "economics",
    "government",
    "literature in english",
    "geography",
    "accounting",
    "commerce",
]

EXAM_TYPES = [
    ("utme", "jamb"),
    ("wassce", "waec"),
]
YEARS = range(2001, 2021)

TOPIC_FALLBACKS = {
    "english-language": "comprehension",
    "mathematics": "algebra",
    "biology": "cell-biology",
    "chemistry": "organic-chemistry",
    "physics": "motion",
    "economics": "price-determination",
    "government": "constitution",
    "literature-in-english": "prose",
    "geography": "map-reading",
    "accounting": "introduction-to-accounting",
    "commerce": "introduction-to-commerce",
}


@dataclass(frozen=True)
class TopicMatch:
    slug: str
    keywords: Tuple[str, ...]


TOPIC_PATTERNS = {
    "english-language": [
        TopicMatch("comprehension", ("comprehension", "passage", "reading")),
        TopicMatch("lexis-and-structure", ("lexis", "structure", "vocabulary", "synonym", "antonym")),
        TopicMatch("oral-english", ("oral", "pronunciation", "stress", "intonation")),
        TopicMatch("grammar", ("grammar", "grammar", "tense")),
        TopicMatch("parts-of-speech", ("noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction")),
        TopicMatch("sentence-structure", ("sentence", "clause", "phrase", "punctuation")),
        TopicMatch("concord", ("concord", "subject verb", "agreement")),
        TopicMatch("tenses", ("tense", "past tense", "present tense", "future tense")),
        TopicMatch("essay-writing", ("essay", "composition")),
        TopicMatch("letter-writing", ("letter", "informal letter", "formal letter")),
        TopicMatch("report-writing", ("report",)),
        TopicMatch("summary-writing", ("summary",)),
    ],
    "mathematics": [
        TopicMatch("number-bases", ("base", "number base")),
        TopicMatch("algebra", ("algebra", "polynomial", "factor", "quadratic", "equation")),
        TopicMatch("indices-and-logarithms", ("index", "logarithm", "log")),
        TopicMatch("sets", ("set", "venn")),
        TopicMatch("fractions-and-percentages", ("fraction", "percentage", "ratio", "proportion")),
        TopicMatch("equations-and-inequalities", ("inequality", "equation", "solve for", "simultaneous")),
        TopicMatch("trigonometry", ("trigonometry", "sin", "cos", "tan")),
        TopicMatch("geometry", ("geometry", "triangle", "circle", "polygon", "angle")),
        TopicMatch("mensuration", ("mensuration", "area", "volume", "surface area")),
        TopicMatch("statistics", ("statistics", "mean", "median", "mode", "range")),
        TopicMatch("probability", ("probability", "chance")),
        TopicMatch("variation", ("variation", "direct variation", "inverse variation")),
    ],
    "biology": [
        TopicMatch("cell-biology", ("cell", "cell biology")),
        TopicMatch("classification-of-living-things", ("classification", "kingdom", "phylum")),
        TopicMatch("ecology", ("ecology", "ecosystem", "habitat", "food chain")),
        TopicMatch("genetics", ("genetics", "gene", "heredity", "dna")),
        TopicMatch("reproduction", ("reproduction", "fertilization", "gamete")),
        TopicMatch("nutrition", ("nutrition", "digestion", "nutrient")),
        TopicMatch("respiration", ("respiration", "respire", "breathing")),
        TopicMatch("excretion", ("excretion", "waste removal")),
        TopicMatch("growth-and-development", ("growth", "development")),
        TopicMatch("nervous-system", ("nervous", "neurons", "brain", "spinal cord")),
        TopicMatch("evolution", ("evolution", "natural selection")),
        TopicMatch("human-physiology", ("human", "physiology", "circulatory", "digestive")),
    ],
    "chemistry": [
        TopicMatch("atomic-structure", ("atomic", "atom", "proton", "electron", "neutron")),
        TopicMatch("chemical-bonding", ("bond", "ionic", "covalent")),
        TopicMatch("periodic-table", ("periodic", "group", "period")),
        TopicMatch("stoichiometry", ("stoichiometry", "mole", "molar")),
        TopicMatch("acids-and-bases", ("acid", "base", "alkali", "ph")),
        TopicMatch("salts", ("salt",)),
        TopicMatch("redox-reactions", ("redox", "oxidation", "reduction")),
        TopicMatch("electrolysis", ("electrolysis", "electrode")),
        TopicMatch("organic-chemistry", ("organic", "alkane", "alkene", "alkyne")),
        TopicMatch("hydrocarbons", ("hydrocarbon",)),
        TopicMatch("chemical-equilibrium", ("equilibrium",)),
        TopicMatch("rates-of-reaction", ("rate", "reaction rate", "catalyst")),
    ],
    "physics": [
        TopicMatch("measurement-and-units", ("measurement", "unit", "dimension")),
        TopicMatch("scalars-and-vectors", ("scalar", "vector")),
        TopicMatch("motion", ("motion", "speed", "velocity", "acceleration")),
        TopicMatch("forces", ("force", "moment", "friction")),
        TopicMatch("work-energy-and-power", ("work", "energy", "power")),
        TopicMatch("heat-energy", ("heat", "temperature", "thermal")),
        TopicMatch("waves", ("wave", "frequency", "wavelength")),
        TopicMatch("light", ("light", "reflection", "refraction", "lens")),
        TopicMatch("sound", ("sound", "echo")),
        TopicMatch("electricity", ("electric", "current", "voltage", "resistance")),
        TopicMatch("magnetism", ("magnet", "magnetic")),
        TopicMatch("modern-physics", ("modern", "radioactivity", "quantum")),
    ],
    "economics": [
        TopicMatch("basic-economic-concepts", ("economic", "economics", "scarcity")),
        TopicMatch("demand", ("demand",)),
        TopicMatch("supply", ("supply",)),
        TopicMatch("price-determination", ("price", "market price")),
        TopicMatch("production", ("production", "cost of production")),
        TopicMatch("market-structures", ("market structure", "competition", "monopoly")),
        TopicMatch("national-income", ("national income", "gdp", "gni")),
        TopicMatch("money-and-banking", ("money", "bank", "banking", "credit")),
        TopicMatch("inflation", ("inflation",)),
        TopicMatch("public-finance", ("tax", "budget", "public finance")),
        TopicMatch("international-trade", ("trade", "export", "import")),
        TopicMatch("economic-development", ("development", "growth")),
    ],
    "government": [
        TopicMatch("meaning-of-government", ("government", "state")),
        TopicMatch("constitution", ("constitution",)),
        TopicMatch("rule-of-law", ("rule of law",)),
        TopicMatch("citizenship", ("citizen", "citizenship")),
        TopicMatch("political-parties", ("political party", "party")),
        TopicMatch("electoral-systems", ("election", "electoral", "voting")),
        TopicMatch("legislature", ("legislature", "parliament", "senate", "house of representatives")),
        TopicMatch("executive", ("executive", "president", "governor")),
        TopicMatch("judiciary", ("judiciary", "court", "judge")),
        TopicMatch("public-administration", ("public administration", "civil service")),
        TopicMatch("local-government", ("local government", "local council")),
        TopicMatch("international-organizations", ("un", "ecowas", "au", "international organization")),
    ],
    "literature-in-english": [
        TopicMatch("poetry", ("poem", "poetry", "poet")),
        TopicMatch("drama", ("drama", "play", "tragedy", "comedy")),
        TopicMatch("prose", ("prose", "novel", "fiction")),
        TopicMatch("literary-appreciation", ("appreciation",)),
        TopicMatch("figures-of-speech", ("metaphor", "simile", "personification", "figure of speech")),
        TopicMatch("themes-and-motifs", ("theme", "motif")),
        TopicMatch("characterization", ("character", "characterization")),
        TopicMatch("plot-structure", ("plot", "plot structure")),
        TopicMatch("setting", ("setting",)),
        TopicMatch("tone-and-mood", ("tone", "mood")),
        TopicMatch("african-literature", ("african literature",)),
        TopicMatch("literary-devices", ("device", "imagery", "symbolism")),
    ],
    "geography": [
        TopicMatch("the-earth", ("earth", "planet")),
        TopicMatch("map-reading", ("map", "scale", "bearing")),
        TopicMatch("surveying", ("survey", "surveying")),
        TopicMatch("rocks", ("rock", "minerals")),
        TopicMatch("weather-and-climate", ("weather", "climate", "rainfall")),
        TopicMatch("vegetation", ("vegetation", "forest", "savanna")),
        TopicMatch("soils", ("soil",)),
        TopicMatch("population-geography", ("population", "census", "demography")),
        TopicMatch("settlement", ("settlement", "urban", "rural")),
        TopicMatch("transportation", ("transport", "transportation", "road", "rail")),
        TopicMatch("industry", ("industry", "industrial")),
        TopicMatch("environmental-conservation", ("environment", "conservation", "pollution")),
    ],
    "accounting": [
        TopicMatch("introduction-to-accounting", ("accounting", "bookkeeping")),
        TopicMatch("accounting-principles", ("principle", "concept")),
        TopicMatch("double-entry-system", ("double entry", "debit", "credit")),
        TopicMatch("cash-book", ("cash book",)),
        TopicMatch("ledger-accounts", ("ledger",)),
        TopicMatch("trial-balance", ("trial balance",)),
        TopicMatch("depreciation", ("depreciation",)),
        TopicMatch("bank-reconciliation", ("bank reconciliation",)),
        TopicMatch("final-accounts", ("final account", "income statement", "balance sheet")),
        TopicMatch("control-accounts", ("control account",)),
        TopicMatch("partnership-accounts", ("partnership",)),
        TopicMatch("company-accounts", ("company account", "share capital")),
    ],
    "commerce": [
        TopicMatch("introduction-to-commerce", ("commerce", "business")),
        TopicMatch("trade", ("trade",)),
        TopicMatch("retail-trade", ("retail",)),
        TopicMatch("wholesale-trade", ("wholesale",)),
        TopicMatch("advertising", ("advertising", "advertisement")),
        TopicMatch("transportation", ("transport",)),
        TopicMatch("warehousing", ("warehouse", "warehousing")),
        TopicMatch("insurance", ("insurance",)),
        TopicMatch("banking", ("bank", "banking")),
        TopicMatch("foreign-trade", ("foreign trade", "import", "export")),
        TopicMatch("business-units", ("business unit", "sole proprietorship", "partnership", "company")),
        TopicMatch("consumer-protection", ("consumer", "protection")),
    ],
}


def main() -> int:
    token = must_env("ALOC_TOKEN")
    db_config = {
        "host": os.getenv("DB_HOST", "localhost"),
        "port": int(os.getenv("DB_PORT", "3306")),
        "database": must_env("DB_NAME"),
        "user": must_env("DB_USER"),
        "password": must_env("DB_PASSWORD"),
    }
    base_url = os.getenv("ALOC_BASE_URL", "https://questions.aloc.com.ng/api/v2").rstrip("/")
    delay_seconds = float(os.getenv("ALOC_DELAY_SECONDS", "1.0"))
    limit = int(os.getenv("ALOC_LIMIT", "120"))

    print("Connecting to database...")
    conn = mysql.connector.connect(**db_config)
    conn.autocommit = False

    try:
        cursor = conn.cursor(dictionary=True)
        subject_map = load_subjects(cursor)
        exam_type_map = load_exam_types(cursor)
        topic_map = load_topics(cursor)

        inserted = 0
        skipped = 0
        failures = 0

        for aloc_exam_type, db_exam_slug in EXAM_TYPES:
            if db_exam_slug not in exam_type_map:
                print(f"[skip] exam type not found in DB: {db_exam_slug}")
                continue

            for subject_name in SUBJECTS:
                subject = normalize_subject_key(subject_name)
                subject_row = subject_map.get(subject)
                if not subject_row:
                    print(f"[skip] subject not found in DB: {subject_name}")
                    continue

                print(f"[fetch] {aloc_exam_type.upper()} -> {db_exam_slug.upper()} - {subject_name}")
                for year in YEARS:
                    try:
                        questions = fetch_questions(
                            base_url=base_url,
                            token=token,
                            subject=subject_name,
                            exam_type=aloc_exam_type,
                            year=year,
                            limit=limit,
                        )
                    except Exception as exc:
                        failures += 1
                        print(f"[error] {subject_name} {aloc_exam_type} {year}: {exc}")
                        time.sleep(delay_seconds)
                        continue

                    if not questions:
                        print(f"[empty] {subject_name} {aloc_exam_type} {year}")
                        time.sleep(delay_seconds)
                        continue

                    for item in questions:
                        question_text = clean_text(item.get("question") or item.get("question_text"))
                        if not question_text:
                            skipped += 1
                            continue

                        if question_exists(cursor, question_text):
                            skipped += 1
                            continue

                        topic_id = resolve_topic_id(
                            cursor=cursor,
                            topic_map=topic_map,
                            subject_slug=subject,
                            subject_id=subject_row["id"],
                            item=item,
                            question_text=question_text,
                        )

                        if topic_id is None:
                            skipped += 1
                            continue

                        rows_inserted = insert_question(
                            cursor=cursor,
                            exam_type_id=exam_type_map[db_exam_slug],
                            subject_id=subject_row["id"],
                            topic_id=topic_id,
                            year=year,
                            item=item,
                            question_text=question_text,
                        )
                        if rows_inserted:
                            inserted += 1
                        else:
                            skipped += 1

                    conn.commit()
                    print(f"[done] {subject_name} {aloc_exam_type} {year} -> inserted so far: {inserted}")
                    time.sleep(delay_seconds)

        print(json.dumps({
            "inserted": inserted,
            "skipped": skipped,
            "failures": failures,
        }, indent=2))
        return 0
    except Exception as exc:
        conn.rollback()
        print(f"[fatal] {exc}", file=sys.stderr)
        return 1
    finally:
        try:
            conn.close()
        except Exception:
            pass


def must_env(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value


def normalize_subject_key(subject: str) -> str:
    return (
        subject.strip().lower()
        .replace("&", "and")
        .replace("  ", " ")
        .replace(" ", "-")
    )


def load_subjects(cursor) -> Dict[str, Dict[str, Any]]:
    cursor.execute("SELECT id, public_id, name, slug FROM subjects")
    rows = cursor.fetchall()
    return {row["slug"]: row for row in rows}


def load_exam_types(cursor) -> Dict[str, int]:
    cursor.execute("SELECT id, slug FROM exam_types")
    return {row["slug"]: row["id"] for row in cursor.fetchall()}


def load_topics(cursor) -> Dict[Tuple[str, str], Dict[str, Any]]:
    cursor.execute(
        """
        SELECT t.id, t.public_id, t.name, t.slug, s.slug AS subject_slug, s.id AS subject_id
        FROM topics t
        JOIN subjects s ON s.id = t.subject_id
        """
    )
    return {(row["subject_slug"], row["slug"]): row for row in cursor.fetchall()}


def fetch_questions(
    base_url: str,
    token: str,
    subject: str,
    exam_type: str,
    year: int,
    limit: int,
) -> List[Dict[str, Any]]:
    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "AccessToken": token,
    }
    params = {
        "subject": subject,
        "type": exam_type,
        "year": year,
    }

    payload = None
    for url in (f"{base_url}/m/{limit}", f"{base_url}/m"):
        response = requests.get(url, headers=headers, params=params, timeout=60)
        if response.ok:
            payload = response.json()
            break

    if payload is None:
        response.raise_for_status()

    if isinstance(payload, list):
        return payload[:limit]

    if isinstance(payload, dict):
        for key in ("data", "results", "questions"):
            if isinstance(payload.get(key), list):
                return payload[key][:limit]
        return [payload]

    return []


def question_exists(cursor, question_text: str) -> bool:
    cursor.execute(
        "SELECT 1 FROM past_questions WHERE question_text = %s LIMIT 1",
        (question_text,),
    )
    return cursor.fetchone() is not None


def resolve_topic_id(
    cursor,
    topic_map: Dict[Tuple[str, str], Dict[str, Any]],
    subject_slug: str,
    subject_id: int,
    item: Dict[str, Any],
    question_text: str,
) -> Optional[int]:
    topic_candidates = []

    raw_topic = item.get("topic") or item.get("topic_name") or item.get("topicSlug")
    if raw_topic:
        topic_candidates.append(slugify(raw_topic))

    subject_patterns = TOPIC_PATTERNS.get(subject_slug, [])
    text = f"{question_text} {json.dumps(item, ensure_ascii=False)}".lower()
    for pattern in subject_patterns:
        if any(keyword in text for keyword in pattern.keywords):
            topic_candidates.append(pattern.slug)

    fallback = TOPIC_FALLBACKS.get(subject_slug)
    if fallback:
        topic_candidates.append(fallback)

    # Remove duplicates while preserving order
    seen = set()
    ordered_candidates = []
    for candidate in topic_candidates:
        if candidate and candidate not in seen:
            seen.add(candidate)
            ordered_candidates.append(candidate)

    for candidate in ordered_candidates:
        row = topic_map.get((subject_slug, candidate))
        if row:
            return row["id"]

    cursor.execute(
        """
        SELECT id
        FROM topics
        WHERE subject_id = %s
        ORDER BY id ASC
        LIMIT 1
        """,
        (subject_id,),
    )
    row = cursor.fetchone()
    return row["id"] if row else None


def insert_question(
    cursor,
    exam_type_id: int,
    subject_id: int,
    topic_id: int,
    year: int,
    item: Dict[str, Any],
    question_text: str,
) -> int:
    options = item.get("option") or item.get("options") or {}
    answer = (item.get("answer") or item.get("correct_option") or "").strip().upper()

    cursor.execute(
        """
        INSERT INTO past_questions (
            public_id, exam_type_id, subject_id, topic_id, exam_year,
            question_text, option_a, option_b, option_c, option_d,
            correct_option, explanation
        )
        SELECT UUID(), %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
        WHERE NOT EXISTS (
            SELECT 1
            FROM past_questions
            WHERE question_text = %s
        )
        """,
        (
            exam_type_id,
            subject_id,
            topic_id,
            year,
            question_text,
            normalize_option(options, "a"),
            normalize_option(options, "b"),
            normalize_option(options, "c"),
            normalize_option(options, "d"),
            answer[:1] if answer else "A",
            clean_text(item.get("explanation") or item.get("solution")),
            question_text,
        ),
    )

    return cursor.rowcount


def normalize_option(options: Dict[str, Any], key: str) -> str:
    value = options.get(key)
    if value is None:
        return ""
    text = clean_text(value)
    return text if text is not None else ""


def slugify(value: str) -> str:
    value = clean_text(value).lower()
    value = value.replace("&", "and")
    value = re.sub(r"[^a-z0-9]+", "-", value)
    return value.strip("-")


def clean_text(value: Any) -> Optional[str]:
    if value is None:
        return None
    text = str(value).strip()
    return text if text else None


if __name__ == "__main__":
    raise SystemExit(main())
