import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ALocSeedRunner {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private static final String BASE_URL = env("ALOC_BASE_URL", "https://questions.aloc.com.ng/api/v2");
    private static final int LIMIT = Integer.parseInt(env("ALOC_LIMIT", "60"));
    private static final int START_YEAR = Integer.parseInt(env("ALOC_START_YEAR", "2001"));
    private static final int END_YEAR = Integer.parseInt(env("ALOC_END_YEAR", "2020"));
    private static final int REQUEST_TIMEOUT_SECONDS = Integer.parseInt(env("ALOC_TIMEOUT_SECONDS", "30"));
    private static final Set<String> EXAM_FILTER = csvEnv("ALOC_EXAM_TYPES");
    private static final Set<String> SUBJECT_FILTER = csvEnv("ALOC_SUBJECTS");

    private static final List<ExamTypeTarget> EXAM_TYPES = List.of(
            new ExamTypeTarget("utme", "jamb"),
            new ExamTypeTarget("wassce", "waec")
    );

    private static final List<SubjectTarget> SUBJECTS = List.of(
            new SubjectTarget("mathematics", "mathematics"),
            new SubjectTarget("biology", "biology"),
            new SubjectTarget("chemistry", "chemistry"),
            new SubjectTarget("physics", "physics"),
            new SubjectTarget("economics", "economics"),
            new SubjectTarget("government", "government"),
            new SubjectTarget("literature in english", "literature-in-english"),
            new SubjectTarget("geography", "geography"),
            new SubjectTarget("accounting", "accounting"),
            new SubjectTarget("commerce", "commerce"),
            new SubjectTarget("english", "english-language")
    );

    private static final Map<String, String> TOPIC_FALLBACKS = Map.ofEntries(
            Map.entry("english-language", "comprehension"),
            Map.entry("mathematics", "algebra"),
            Map.entry("biology", "cell-biology"),
            Map.entry("chemistry", "organic-chemistry"),
            Map.entry("physics", "motion"),
            Map.entry("economics", "price-determination"),
            Map.entry("government", "constitution"),
            Map.entry("literature-in-english", "prose"),
            Map.entry("geography", "map-reading"),
            Map.entry("accounting", "introduction-to-accounting"),
            Map.entry("commerce", "introduction-to-commerce")
    );

    private static final Map<String, List<TopicMatch>> TOPIC_PATTERNS = Map.ofEntries(

            // ─── ENGLISH LANGUAGE ────────────────────────────────────────────────────
            Map.entry("english-language", List.of(
                    new TopicMatch("comprehension", "comprehension", "read the passage", "passage below", "from the passage"),
                    new TopicMatch("lexis-and-structure", "lexis", "vocabulary", "synonym", "antonym", "word closest", "word opposite", "nearest in meaning"),
                    new TopicMatch("oral-english", "oral", "pronunciation", "stress pattern", "intonation", "phoneme", "rhyme"),
                    new TopicMatch("parts-of-speech", "noun", "pronoun", "adjective", "adverb", "preposition", "conjunction", "interjection", "parts of speech"),
                    new TopicMatch("sentence-structure", "sentence", "clause", "phrase", "punctuation", "punctuate"),
                    new TopicMatch("concord", "concord", "subject-verb agreement", "subject verb agreement"),
                    new TopicMatch("tenses", "tense", "past tense", "present tense", "future tense", "past perfect", "present perfect"),
                    new TopicMatch("grammar", "grammar", "grammatically"),
                    new TopicMatch("essay-writing", "essay", "composition", "expository", "narrative essay"),
                    new TopicMatch("letter-writing", "letter writing", "informal letter", "formal letter", "write a letter"),
                    new TopicMatch("report-writing", "report writing", "write a report"),
                    new TopicMatch("summary-writing", "summary writing", "summarize", "summarise")
            )),

            // ─── MATHEMATICS ─────────────────────────────────────────────────────────
            Map.entry("mathematics", List.of(
                    // number-bases: only specific keywords, NOT generic "base"
                    new TopicMatch("number-bases", "number base", "base 2", "base 8", "base 10", "base 16",
                            "binary", "octal", "hexadecimal", "convert to base", "express in base",
                            "denary", "decimal equivalent of", "11011", "10110"),
                    new TopicMatch("indices-and-logarithms", "logarithm", "log ", "log(", "indices", "index notation",
                            "laws of indices", "antilog", "logartihm"),
                    new TopicMatch("sets", "set notation", "venn diagram", "universal set", "intersection",
                            "union of set", "complement of", "n(a∩b)", "n(a∪b)", "element of a set"),
                    new TopicMatch("fractions-and-percentages", "percentage", "fraction", "ratio", "proportion",
                            "simple interest", "profit and loss", "discount", "commission", "hire purchase"),
                    new TopicMatch("equations-and-inequalities", "inequality", "solve for x", "simultaneous equation",
                            "linear equation", "quadratic equation", "roots of", "completing the square"),
                    new TopicMatch("algebra", "algebra", "polynomial", "factori", "expand", "simplify",
                            "expression", "coefficient", "binomial theorem", "arithmetic progression",
                            "geometric progression", "sequence", "series"),
                    new TopicMatch("trigonometry", "trigonometry", "sin ", "cos ", "tan ", "sine", "cosine",
                            "tangent", "angle of elevation", "angle of depression", "bearing", "pythagoras"),
                    new TopicMatch("geometry", "triangle", "circle", "polygon", "quadrilateral", "parallelogram",
                            "rhombus", "trapezium", "isosceles", "equilateral", "exterior angle",
                            "interior angle", "locus", "construction"),
                    new TopicMatch("mensuration", "mensuration", "area of", "volume of", "surface area",
                            "perimeter", "circumference", "sector", "segment of a circle", "curved surface"),
                    new TopicMatch("statistics", "statistics", "mean", "median", "mode", "frequency table",
                            "histogram", "bar chart", "pie chart", "ogive", "cumulative frequency", "range of"),
                    new TopicMatch("probability", "probability", "chance", "likely", "random", "sample space",
                            "event", "independent event", "mutually exclusive"),
                    new TopicMatch("variation", "direct variation", "inverse variation", "joint variation",
                            "partial variation", "varies directly", "varies inversely")
            )),

            // ─── BIOLOGY ─────────────────────────────────────────────────────────────
            Map.entry("biology", List.of(
                    new TopicMatch("cell-biology", "cell membrane", "cell wall", "nucleus", "cytoplasm",
                            "mitochondria", "chloroplast", "cell division", "mitosis", "meiosis",
                            "cell organelle", "osmosis", "diffusion", "prokaryotic", "eukaryotic"),
                    new TopicMatch("classification-of-living-things", "classification", "kingdom", "phylum",
                            "class", "order", "family", "genus", "species", "taxonomy", "binomial",
                            "vertebrate", "invertebrate", "mammal", "reptile", "amphibian"),
                    new TopicMatch("ecology", "ecology", "ecosystem", "habitat", "food chain", "food web",
                            "producer", "consumer", "decomposer", "biome", "population", "community",
                            "symbiosis", "parasitism", "mutualism", "commensalism", "succession"),
                    new TopicMatch("genetics", "genetics", "gene", "allele", "heredity", "dna", "rna",
                            "chromosome", "dominant", "recessive", "genotype", "phenotype", "mutation",
                            "mendel", "monohybrid", "dihybrid", "punnett"),
                    new TopicMatch("reproduction", "reproduction", "fertilization", "gamete", "ovulation",
                            "menstrual", "pregnancy", "embryo", "foetus", "pollination", "seed dispersal",
                            "sexual reproduction", "asexual reproduction", "vegetative propagation"),
                    new TopicMatch("nutrition", "nutrition", "digestion", "nutrient", "carbohydrate", "protein",
                            "fat", "vitamin", "mineral", "enzyme", "bile", "stomach", "small intestine",
                            "large intestine", "absorption", "assimilation", "malnutrition"),
                    new TopicMatch("respiration", "respiration", "breathing", "lung", "alveoli", "oxygen",
                            "carbon dioxide", "aerobic", "anaerobic", "diaphragm", "trachea", "bronchus"),
                    new TopicMatch("excretion", "excretion", "kidney", "nephron", "urine", "sweat",
                            "urea", "liver function", "osmoregulation", "dialysis"),
                    new TopicMatch("nervous-system", "nervous system", "neuron", "brain", "spinal cord",
                            "reflex action", "synapse", "nerve impulse", "sense organ", "receptor",
                            "eye", "ear", "hormone", "endocrine"),
                    new TopicMatch("growth-and-development", "growth", "germination", "metamorphosis",
                            "development", "primary growth", "secondary growth"),
                    new TopicMatch("evolution", "evolution", "natural selection", "adaptation", "darwin",
                            "fossil", "variation", "speciation", "survival of the fittest"),
                    new TopicMatch("human-physiology", "blood group", "blood type", "circulatory system",
                            "heart", "artery", "vein", "capillary", "haemoglobin", "immune system",
                            "skeletal system", "muscle", "joint")
            )),

            // ─── CHEMISTRY ───────────────────────────────────────────────────────────
            Map.entry("chemistry", List.of(
                    new TopicMatch("atomic-structure", "atomic number", "mass number", "electron configuration",
                            "proton", "neutron", "electron", "isotope", "bohr", "orbital", "shell"),
                    new TopicMatch("chemical-bonding", "ionic bond", "covalent bond", "metallic bond",
                            "hydrogen bond", "van der waals", "electronegativity", "bond angle", "hybridization"),
                    new TopicMatch("periodic-table", "periodic table", "period 2", "period 3", "group i",
                            "group ii", "group vii", "alkali metal", "halogen", "noble gas", "transition metal"),
                    new TopicMatch("stoichiometry", "stoichiometry", "mole", "molar mass", "avogadro",
                            "empirical formula", "molecular formula", "percentage composition", "limiting reagent"),
                    // acids-and-bases: "acid" and "base" but NOT generic standalone "base"
                    new TopicMatch("acids-and-bases", "acid-base", "acids and bases", "ph value", "ph scale",
                            "strong acid", "weak acid", "alkali", "neutralization", "buffer solution",
                            "indicator", "litmus", "hydrochloric acid", "sulphuric acid", "nitric acid"),
                    new TopicMatch("salts", "salt formation", "types of salt", "normal salt", "acid salt",
                            "basic salt", "double salt", "complex salt", "solubility of salt"),
                    new TopicMatch("redox-reactions", "redox", "oxidation number", "oxidation state",
                            "reducing agent", "oxidizing agent", "half equation", "electron transfer"),
                    new TopicMatch("electrolysis", "electrolysis", "electrolyte", "electrode", "cathode",
                            "anode", "faraday", "electroplating", "discharge of ions"),
                    new TopicMatch("organic-chemistry", "organic chemistry", "alkane", "alkene", "alkyne",
                            "functional group", "isomerism", "homologous series", "ester", "alcohol",
                            "carboxylic acid", "aldehyde", "ketone", "amine", "amide", "polymer",
                            "addition reaction", "substitution reaction", "fermentation"),
                    new TopicMatch("hydrocarbons", "hydrocarbon", "methane", "ethane", "propane", "butane",
                            "ethene", "ethyne", "benzene", "aromatic", "aliphatic"),
                    new TopicMatch("chemical-equilibrium", "equilibrium", "le chatelier", "equilibrium constant",
                            "haber process", "contact process", "reversible reaction"),
                    new TopicMatch("rates-of-reaction", "rate of reaction", "reaction rate", "catalyst",
                            "activation energy", "collision theory", "temperature effect on rate")
            )),

            // ─── PHYSICS ─────────────────────────────────────────────────────────────
            Map.entry("physics", List.of(
                    new TopicMatch("measurement-and-units", "si unit", "base unit", "derived unit",
                            "measurement", "significant figure", "error", "dimension analysis",
                            "vernier caliper", "micrometer", "screw gauge"),
                    new TopicMatch("scalars-and-vectors", "scalar quantity", "vector quantity", "resultant",
                            "resolution of vector", "triangle of forces", "parallelogram law"),
                    new TopicMatch("motion", "velocity", "acceleration", "speed", "displacement",
                            "equations of motion", "projectile", "circular motion", "uniform motion",
                            "newton's law of motion", "inertia", "momentum", "impulse"),
                    new TopicMatch("forces", "force", "friction", "tension", "normal reaction", "weight",
                            "moment of a force", "torque", "equilibrium of forces", "centre of gravity",
                            "archimedes", "upthrust", "pressure"),
                    new TopicMatch("work-energy-and-power", "work done", "kinetic energy", "potential energy",
                            "conservation of energy", "power output", "efficiency", "mechanical advantage",
                            "velocity ratio", "machine"),
                    new TopicMatch("heat-energy", "heat", "temperature", "thermometer", "specific heat capacity",
                            "latent heat", "thermal expansion", "conduction", "convection", "radiation",
                            "gas law", "boyle", "charles", "absolute zero"),
                    new TopicMatch("waves", "wave", "frequency", "wavelength", "amplitude", "period",
                            "transverse wave", "longitudinal wave", "standing wave", "interference",
                            "diffraction", "doppler effect"),
                    new TopicMatch("light", "reflection", "refraction", "lens", "mirror", "focal length",
                            "refractive index", "total internal reflection", "dispersion", "spectrum",
                            "optical instrument", "telescope", "microscope"),
                    new TopicMatch("sound", "sound wave", "echo", "resonance", "pitch", "loudness",
                            "ultrasound", "speed of sound", "decibel"),
                    new TopicMatch("electricity", "electric current", "voltage", "resistance", "ohm's law",
                            "series circuit", "parallel circuit", "electromotive force", "capacitor",
                            "electric field", "coulomb", "power dissipated"),
                    new TopicMatch("magnetism", "magnetic field", "electromagnet", "solenoid", "transformer",
                            "electric motor", "generator", "faraday's law", "lenz's law", "magnetic flux"),
                    new TopicMatch("modern-physics", "radioactivity", "alpha particle", "beta particle",
                            "gamma ray", "half-life", "nuclear fission", "nuclear fusion", "photoelectric",
                            "quantum", "x-ray", "cathode ray")
            )),

            // ─── ECONOMICS ───────────────────────────────────────────────────────────
            Map.entry("economics", List.of(
                    new TopicMatch("basic-economic-concepts", "scarcity", "opportunity cost", "scale of preference",
                            "factors of production", "division of labour", "economic system",
                            "mixed economy", "free market", "planned economy"),
                    new TopicMatch("demand", "law of demand", "demand curve", "effective demand",
                            "elasticity of demand", "price elasticity", "income elasticity",
                            "substitute goods", "complementary goods", "change in demand"),
                    new TopicMatch("supply", "law of supply", "supply curve", "elasticity of supply",
                            "change in supply", "producer surplus", "supply schedule"),
                    new TopicMatch("price-determination", "equilibrium price", "market equilibrium",
                            "price mechanism", "price floor", "price ceiling", "market forces",
                            "excess demand", "excess supply"),
                    new TopicMatch("production", "factors of production", "cost of production",
                            "total cost", "marginal cost", "average cost", "fixed cost", "variable cost",
                            "economies of scale", "returns to scale", "production possibility"),
                    new TopicMatch("market-structures", "perfect competition", "monopoly", "oligopoly",
                            "monopolistic competition", "market structure", "price discrimination",
                            "cartel", "duopoly"),
                    new TopicMatch("national-income", "gdp", "gnp", "gni", "national income",
                            "per capita income", "standard of living", "circular flow of income"),
                    new TopicMatch("money-and-banking", "money supply", "central bank", "commercial bank",
                            "credit creation", "monetary policy", "interest rate", "inflation rate",
                            "exchange rate", "currency"),
                    new TopicMatch("inflation", "inflation", "deflation", "cost-push", "demand-pull",
                            "hyperinflation", "consumer price index", "purchasing power"),
                    new TopicMatch("public-finance", "government revenue", "government expenditure",
                            "taxation", "direct tax", "indirect tax", "budget deficit", "public debt",
                            "fiscal policy", "value added tax"),
                    new TopicMatch("international-trade", "export", "import", "balance of trade",
                            "balance of payments", "terms of trade", "comparative advantage",
                            "tariff", "quota", "free trade"),
                    new TopicMatch("economic-development", "economic development", "economic growth",
                            "developing country", "underdevelopment", "foreign aid", "structural adjustment")
            )),

            // ─── GOVERNMENT ──────────────────────────────────────────────────────────
            Map.entry("government", List.of(
                    new TopicMatch("constitution", "constitution", "constitutional", "fundamental rights",
                            "bill of rights", "supremacy of constitution", "constitutional provision"),
                    new TopicMatch("rule-of-law", "rule of law", "equality before the law", "dicey",
                            "due process", "supremacy of law"),
                    new TopicMatch("citizenship", "citizenship", "naturalization", "civic rights",
                            "civic duties", "citizen", "nationality"),
                    new TopicMatch("political-parties", "political party", "multiparty", "one-party",
                            "party system", "party manifesto", "pressure group", "interest group"),
                    new TopicMatch("electoral-systems", "election", "voting", "suffrage", "franchise",
                            "electoral commission", "proportional representation", "first past the post",
                            "inec", "ballot", "constituency"),
                    new TopicMatch("legislature", "legislature", "parliament", "senate", "house of representatives",
                            "national assembly", "bill", "law making", "bicameral", "unicameral",
                            "legislative arm", "law making process"),
                    new TopicMatch("executive", "executive", "president", "prime minister", "governor",
                            "cabinet", "council of ministers", "head of state", "head of government"),
                    new TopicMatch("judiciary", "judiciary", "court", "judge", "supreme court",
                            "court of appeal", "magistrate", "judicial review", "separation of powers"),
                    new TopicMatch("public-administration", "civil service", "public service", "bureaucracy",
                            "public corporation", "parastatals", "government agency"),
                    new TopicMatch("local-government", "local government", "local council", "grassroots",
                            "chieftaincy", "traditional ruler", "local authority"),
                    new TopicMatch("international-organizations", "united nations", "ecowas", "african union",
                            "commonwealth", "nato", "opec", "international organization", "world bank", "imf"),
                    new TopicMatch("meaning-of-government", "sovereignty", "state", "nation", "legitimacy",
                            "authority", "power", "governance", "political science")
            )),

            // ─── LITERATURE IN ENGLISH ───────────────────────────────────────────────
            Map.entry("literature-in-english", List.of(
                    new TopicMatch("poetry", "poem", "poetry", "poet", "stanza", "verse", "rhyme scheme",
                            "lyric", "ballad", "sonnet", "ode", "epic poem"),
                    new TopicMatch("drama", "drama", "playwright", "tragedy", "comedy", "act", "scene",
                            "stage direction", "protagonist", "antagonist", "soliloquy", "aside"),
                    new TopicMatch("prose", "novel", "short story", "fiction", "narrator", "narrative",
                            "plot summary", "chapter"),
                    new TopicMatch("figures-of-speech", "metaphor", "simile", "personification",
                            "hyperbole", "irony", "alliteration", "onomatopoeia", "oxymoron",
                            "figure of speech", "rhetorical"),
                    new TopicMatch("themes-and-motifs", "theme", "motif", "subject matter", "central idea"),
                    new TopicMatch("characterization", "character", "characterization", "protagonist",
                            "round character", "flat character", "dynamic character"),
                    new TopicMatch("plot-structure", "plot", "climax", "resolution", "exposition",
                            "rising action", "falling action", "denouement", "conflict"),
                    new TopicMatch("setting", "setting", "time and place", "atmosphere", "environment of the story"),
                    new TopicMatch("tone-and-mood", "tone", "mood", "attitude of the poet", "writer's tone"),
                    new TopicMatch("african-literature", "chinua achebe", "wole soyinka", "ngugi",
                            "african writer", "african literature", "post-colonial"),
                    new TopicMatch("literary-devices", "imagery", "symbolism", "flashback", "foreshadowing",
                            "stream of consciousness", "allegory", "satire", "literary device")
            )),

            // ─── GEOGRAPHY ───────────────────────────────────────────────────────────
            Map.entry("geography", List.of(
                    new TopicMatch("the-earth", "earth's structure", "crust", "mantle", "core",
                            "tectonic plate", "earthquake", "volcano", "fold mountain", "rift valley"),
                    new TopicMatch("map-reading", "map scale", "contour", "grid reference", "bearing",
                            "map projection", "topographic map", "relief map", "map symbol"),
                    new TopicMatch("surveying", "chain survey", "plane table", "levelling", "surveying instrument"),
                    new TopicMatch("rocks", "igneous rock", "sedimentary rock", "metamorphic rock",
                            "rock cycle", "mineral", "weathering", "erosion", "deposition"),
                    new TopicMatch("weather-and-climate", "rainfall", "temperature", "humidity", "wind",
                            "pressure belt", "climate zone", "tropical climate", "mediterranean climate",
                            "weather instrument", "thermometer", "barometer"),
                    new TopicMatch("vegetation", "tropical rainforest", "savanna", "desert", "mangrove",
                            "vegetation zone", "forest", "grassland", "shrub"),
                    new TopicMatch("soils", "soil formation", "soil profile", "laterite", "alluvial soil",
                            "soil erosion", "soil conservation", "humus", "soil texture"),
                    new TopicMatch("population-geography", "population density", "census", "birth rate",
                            "death rate", "migration", "urbanization", "overpopulation", "demography"),
                    new TopicMatch("settlement", "urban settlement", "rural settlement", "site and situation",
                            "urban growth", "suburbanization", "shanty town"),
                    new TopicMatch("transportation", "road transport", "rail transport", "water transport",
                            "air transport", "pipeline", "transport network"),
                    new TopicMatch("industry", "manufacturing industry", "industrial location", "raw material",
                            "agro-industry", "cottage industry", "heavy industry"),
                    new TopicMatch("environmental-conservation", "pollution", "deforestation", "desertification",
                            "conservation", "afforestation", "environmental degradation", "global warming")
            )),

            // ─── ACCOUNTING ──────────────────────────────────────────────────────────
            Map.entry("accounting", List.of(
                    new TopicMatch("double-entry-system", "double entry", "debit", "credit", "t-account",
                            "dual aspect", "debit side", "credit side"),
                    new TopicMatch("cash-book", "cash book", "petty cash", "cash receipt", "cash payment",
                            "three-column cash book", "two-column cash book"),
                    new TopicMatch("ledger-accounts", "ledger", "posting", "general ledger",
                            "sales ledger", "purchase ledger", "nominal ledger"),
                    new TopicMatch("trial-balance", "trial balance", "balance agreement", "errors in trial balance"),
                    new TopicMatch("depreciation", "depreciation", "straight line method", "reducing balance",
                            "accumulated depreciation", "book value", "provision for depreciation"),
                    new TopicMatch("bank-reconciliation", "bank reconciliation", "bank statement",
                            "unpresented cheque", "outstanding deposit", "bank charges"),
                    new TopicMatch("final-accounts", "income statement", "balance sheet", "profit and loss",
                            "trading account", "gross profit", "net profit", "statement of affairs"),
                    new TopicMatch("control-accounts", "control account", "debtors control", "creditors control",
                            "sales ledger control", "purchase ledger control"),
                    new TopicMatch("partnership-accounts", "partnership", "profit sharing ratio",
                            "goodwill", "admission of partner", "dissolution of partnership",
                            "current account", "capital account"),
                    new TopicMatch("company-accounts", "share capital", "ordinary share", "preference share",
                            "debenture", "dividend", "retained earnings", "shareholders fund"),
                    new TopicMatch("accounting-principles", "accounting concept", "going concern",
                            "accrual concept", "prudence concept", "consistency principle",
                            "materiality", "matching principle"),
                    new TopicMatch("introduction-to-accounting", "bookkeeping", "source document",
                            "invoice", "receipt", "voucher", "journal entry", "books of account")
            )),

            // ─── COMMERCE ────────────────────────────────────────────────────────────
            Map.entry("commerce", List.of(
                    new TopicMatch("retail-trade", "retailer", "retail trade", "supermarket",
                            "departmental store", "chain store", "cooperative society", "market"),
                    new TopicMatch("wholesale-trade", "wholesaler", "wholesale trade", "middleman",
                            "distributor", "bulk breaking"),
                    new TopicMatch("advertising", "advertising", "advertisement", "mass media",
                            "publicity", "sales promotion", "branding"),
                    new TopicMatch("warehousing", "warehouse", "warehousing", "bonded warehouse",
                            "cold storage", "storage of goods"),
                    new TopicMatch("insurance", "insurance", "premium", "policy", "indemnity",
                            "insurable interest", "subrogation", "underwriter", "life insurance",
                            "fire insurance", "marine insurance"),
                    new TopicMatch("banking", "commercial bank", "central bank", "savings account",
                            "current account", "cheque", "overdraft", "loan", "credit facility"),
                    new TopicMatch("transportation", "freight", "bill of lading", "consignment",
                            "carriage of goods", "means of transport"),
                    new TopicMatch("foreign-trade", "foreign trade", "balance of trade", "import duty",
                            "export duty", "letter of credit", "bill of exchange", "customs"),
                    new TopicMatch("business-units", "sole trader", "sole proprietorship", "limited liability",
                            "public limited company", "private limited company", "cooperative",
                            "public corporation", "nationalisation"),
                    new TopicMatch("consumer-protection", "consumer protection", "consumer rights",
                            "standards organisation", "son", "consumer association", "trade description"),
                    new TopicMatch("trade", "home trade", "domestic trade", "entrepot trade",
                            "visible trade", "invisible trade"),
                    new TopicMatch("introduction-to-commerce", "commerce", "channels of distribution",
                            "aids to trade", "functions of commerce", "importance of commerce")
            ))
    );

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java ALocSeedRunner <secrets-file-path>");
        }

        String attachmentText = Files.readString(Path.of(args[0]));
        String token = requireSecret(firstMatch(attachmentText, "ALOC-[A-Za-z0-9]+"), "ALOC token");
        String password = requireSecret(firstMatch(attachmentText, "DB_PASSWORD='([^']+)'", 1), "DB password");

        String dbUrl = env("DB_URL", "jdbc:mysql://localhost:3306/academicai?createDatabaseIfNotExist=true");
        String dbUser = env("DB_USERNAME", "root");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, password)) {
            conn.setAutoCommit(false);

            Map<String, Long> examTypeIds = loadExamTypeIds(conn);
            Map<String, SubjectRow> subjects = loadSubjects(conn);
            Map<TopicKey, Long> topics = loadTopics(conn);
            Set<String> existingQuestionTexts = loadExistingQuestionTexts(conn);
            log("[info] loaded existing question texts: %d%n", existingQuestionTexts.size());

            int seen = 0;
            int inserted = 0;
            int skipped = 0;
            int failures = 0;

            for (ExamTypeTarget examType : EXAM_TYPES) {
                if (!matchesExamFilter(examType)) continue;

                Long examTypeId = examTypeIds.get(examType.dbSlug());
                if (examTypeId == null) {
                    log("[skip] missing exam type: %s%n", examType.dbSlug());
                    continue;
                }

                for (SubjectTarget subjectTarget : SUBJECTS) {
                    if (!matchesSubjectFilter(subjectTarget)) continue;

                    SubjectRow subject = subjects.get(subjectTarget.dbSlug());
                    if (subject == null) {
                        log("[skip] missing subject: %s%n", subjectTarget.dbSlug());
                        continue;
                    }

                    for (int year = START_YEAR; year <= END_YEAR; year++) {
                        try {
                            List<JsonNode> questions = fetchQuestions(token, examType.alocType(), subjectTarget.alocSubject(), year);
                            if (questions.isEmpty()) {
                                log("[empty] %s %s %d%n", examType.alocType(), subject.slug(), year);
                                continue;
                            }

                            int yearInserted = 0;
                            for (JsonNode question : questions) {
                                seen++;
                                InsertResult result = insertQuestion(
                                        conn, examTypeId, subject, topics,
                                        existingQuestionTexts, year, question
                                );
                                if (result == InsertResult.INSERTED) {
                                    inserted++;
                                    yearInserted++;
                                } else {
                                    skipped++;
                                }
                            }
                            conn.commit();
                            log("[done] %s %s %d fetched=%d inserted=%d total_inserted=%d skipped=%d%n",
                                    examType.alocType(), subject.slug(), year,
                                    questions.size(), yearInserted, inserted, skipped);

                        } catch (Exception exception) {
                            failures++;
                            conn.rollback();
                            log("[error] %s %s %d: %s%n",
                                    examType.alocType(), subject.slug(), year, describe(exception));
                        }
                    }
                }
            }

            log("{\"seen\":%d,\"inserted\":%d,\"skipped\":%d,\"failures\":%d}%n",
                    seen, inserted, skipped, failures);
        }
    }

    private static List<JsonNode> fetchQuestions(String token, String examType, String subject, int year) throws Exception {
        String url = BASE_URL + "/m/" + LIMIT
                + "?subject=" + encode(subject)
                + "&type=" + encode(examType)
                + "&year=" + year;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("AccessToken", token)
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("ALOC status " + response.statusCode());
        }

        JsonNode root = JSON.readTree(response.body());
        JsonNode data = root.isArray() ? root : root.path("data");
        if (!data.isArray()) return List.of();

        List<JsonNode> questions = new ArrayList<>();
        data.forEach(questions::add);
        return questions;
    }

    private static InsertResult insertQuestion(
            Connection conn,
            long examTypeId,
            SubjectRow subject,
            Map<TopicKey, Long> topics,
            Set<String> existingQuestionTexts,
            int year,
            JsonNode item
    ) throws SQLException {
        String questionText = text(item, "question", "question_text");
        if (questionText == null || questionText.isBlank()) return InsertResult.SKIPPED;

        String storedQuestionText = truncate(questionText, 2000);
        if (existingQuestionTexts.contains(storedQuestionText)) return InsertResult.SKIPPED;

        Long topicId = resolveTopicId(conn, topics, subject, item, questionText);
        if (topicId == null) return InsertResult.SKIPPED;

        String sql = """
                INSERT INTO past_questions (
                    public_id, exam_type_id, subject_id, topic_id, exam_year,
                    question_text, option_a, option_b, option_c, option_d,
                    correct_option, explanation
                )
                VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, examTypeId);
            ps.setLong(2, subject.id());
            ps.setLong(3, topicId);
            ps.setInt(4, year);
            ps.setString(5, storedQuestionText);
            ps.setString(6, truncate(option(item, "a"), 1000));
            ps.setString(7, truncate(option(item, "b"), 1000));
            ps.setString(8, truncate(option(item, "c"), 1000));
            ps.setString(9, truncate(option(item, "d"), 1000));
            ps.setString(10, normalizeAnswer(text(item, "answer", "correct_option")));
            ps.setString(11, truncate(text(item, "explanation", "solution"), 2000));
            if (ps.executeUpdate() == 1) {
                existingQuestionTexts.add(storedQuestionText);
                return InsertResult.INSERTED;
            }
            return InsertResult.SKIPPED;
        }
    }

    private static Long resolveTopicId(
            Connection conn,
            Map<TopicKey, Long> topics,
            SubjectRow subject,
            JsonNode item,
            String questionText
    ) throws SQLException {
        Set<String> candidates = new LinkedHashSet<>();

        // 1. Use topic field from API if present
        String rawTopic = text(item, "topic", "topic_name", "topicSlug");
        if (rawTopic != null) candidates.add(slugify(rawTopic));

        // 2. Match against keyword patterns
        String searchable = (questionText + " " + item.toString()).toLowerCase(Locale.ROOT);
        for (TopicMatch match : TOPIC_PATTERNS.getOrDefault(subject.slug(), List.of())) {
            if (match.matches(searchable)) candidates.add(match.slug());
        }

        // 3. Subject fallback topic
        String fallback = TOPIC_FALLBACKS.get(subject.slug());
        if (fallback != null) candidates.add(fallback);

        // 4. Try each candidate against the topics map
        for (String candidate : candidates) {
            Long topicId = topics.get(new TopicKey(subject.slug(), candidate));
            if (topicId != null) return topicId;
        }

        // 5. Last resort: first topic for this subject
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM topics WHERE subject_id = ? ORDER BY id ASC LIMIT 1")) {
            ps.setLong(1, subject.id());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        }
    }

    // ─── DB loaders ──────────────────────────────────────────────────────────

    private static Map<String, Long> loadExamTypeIds(Connection conn) throws SQLException {
        Map<String, Long> rows = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, slug FROM exam_types");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.put(rs.getString("slug"), rs.getLong("id"));
        }
        return rows;
    }

    private static Map<String, SubjectRow> loadSubjects(Connection conn) throws SQLException {
        Map<String, SubjectRow> rows = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, slug FROM subjects");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                rows.put(rs.getString("slug"), new SubjectRow(rs.getLong("id"), rs.getString("slug")));
        }
        return rows;
    }

    private static Map<TopicKey, Long> loadTopics(Connection conn) throws SQLException {
        Map<TopicKey, Long> rows = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT t.id, t.slug, s.slug AS subject_slug
                FROM topics t
                JOIN subjects s ON s.id = t.subject_id
                """);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                rows.put(new TopicKey(rs.getString("subject_slug"), rs.getString("slug")), rs.getLong("id"));
        }
        return rows;
    }

    private static Set<String> loadExistingQuestionTexts(Connection conn) throws SQLException {
        Set<String> rows = new LinkedHashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT question_text FROM past_questions");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(rs.getString("question_text"));
        }
        return rows;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static String text(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (!value.isMissingNode() && !value.isNull()) {
                String text = value.asText().trim();
                if (!text.isBlank()) return text;
            }
        }
        return null;
    }

    private static String option(JsonNode node, String key) {
        JsonNode options = node.path("option");
        if (options.isMissingNode() || options.isNull()) options = node.path("options");
        String value = options.path(key).asText("").trim();
        return value.isBlank() ? "" : value;
    }

    private static String normalizeAnswer(String answer) {
        if (answer == null || answer.isBlank()) return "A";
        return answer.trim().substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String slugify(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("&", "and")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }

    private static String firstMatch(String text, String pattern) {
        return firstMatch(text, pattern, 0);
    }

    private static String firstMatch(String text, String pattern, int group) {
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        return matcher.find() ? matcher.group(group) : null;
    }

    private static String requireSecret(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing " + label);
        return value;
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static Set<String> csvEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) return Set.of();
        Set<String> items = new LinkedHashSet<>();
        for (String item : value.split(",")) {
            String normalized = item.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isBlank()) items.add(normalized);
        }
        return items;
    }

    private static boolean matchesExamFilter(ExamTypeTarget examType) {
        return EXAM_FILTER.isEmpty()
                || EXAM_FILTER.contains(examType.alocType())
                || EXAM_FILTER.contains(examType.dbSlug());
    }

    private static boolean matchesSubjectFilter(SubjectTarget subject) {
        return SUBJECT_FILTER.isEmpty()
                || SUBJECT_FILTER.contains(subject.alocSubject())
                || SUBJECT_FILTER.contains(subject.dbSlug());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void log(String format, Object... args) {
        System.out.printf(format, args);
        System.out.flush();
    }

    private static String describe(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    // ─── Types ───────────────────────────────────────────────────────────────

    private enum InsertResult { INSERTED, SKIPPED }

    private record SubjectRow(long id, String slug) {}
    private record ExamTypeTarget(String alocType, String dbSlug) {}
    private record SubjectTarget(String alocSubject, String dbSlug) {}
    private record TopicKey(String subjectSlug, String topicSlug) {}

    private record TopicMatch(String slug, List<String> keywords) {
        TopicMatch(String slug, String... keywords) {
            this(slug, List.of(keywords));
        }
        boolean matches(String text) {
            return keywords.stream().anyMatch(text::contains);
        }
    }
}
