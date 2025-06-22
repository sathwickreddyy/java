-- Market Trends Table
CREATE TABLE IF NOT EXISTS market_trends (
     id BIGSERIAL PRIMARY KEY,
     trade_date DATE,
     currency VARCHAR(10),
    exchange_rate DECIMAL(10,4),
    trading_volume BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- // No Primary Key - Bitemporal
CREATE TABLE IF NOT EXISTS market_trends_bi_temporal (
    trade_date DATE,
    currency VARCHAR(10),
    exchange_rate DECIMAL(10,4),
    trading_volume BIGINT,
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP NOT NULL DEFAULT '9999-12-31 00:00:00',
    reporting_date DATE NOT NULL
);

-- Risk Metrics Table
CREATE TABLE IF NOT EXISTS risk_metrics (
                                            id BIGSERIAL PRIMARY KEY,
                                            portfolio_id VARCHAR(50),
    var_95 DECIMAL(15,4),
    expected_shortfall DECIMAL(15,4),
    liquidity_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- API Data Table
CREATE TABLE IF NOT EXISTS api_data (
                                             id BIGSERIAL PRIMARY KEY,
                                             forecast_id VARCHAR(50),
    asset_class VARCHAR(50),
    predicted_liquidity DECIMAL(15,4),
    confidence_level DECIMAL(5,2),
    forecast_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Portfolio Configuration Table
CREATE TABLE IF NOT EXISTS portfolio_config (
                                                id BIGSERIAL PRIMARY KEY,
                                                portfolio_id VARCHAR(50),
    portfolio_name VARCHAR(100),
    risk_profile VARCHAR(20),
    liquidity_threshold DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Data Loading Audit Table
CREATE TABLE IF NOT EXISTS data_loading_audit (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  table_name VARCHAR(50),
    source_type VARCHAR(20),
    record_count INT,
    duration_ms BIGINT,
    execution_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_market_trends_date ON market_trends(trade_date);
CREATE INDEX IF NOT EXISTS idx_market_trends_bitemporal_reporting_date ON market_trends_bi_temporal(reporting_date);
CREATE INDEX IF NOT EXISTS idx_risk_metrics_portfolio ON risk_metrics(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_forecast_data_date ON api_data(forecast_date);
CREATE INDEX IF NOT EXISTS idx_portfolio_config_id ON portfolio_config(portfolio_id);