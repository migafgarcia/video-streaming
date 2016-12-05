module shared {
    struct Measurement {
        string tower; // tower id
        float windSpeed; // knots
        short windDirection; // degrees
        float temperature; // degrees Celsius
    };

    interface Monitor {
        void report(Measurement m);
    };
};