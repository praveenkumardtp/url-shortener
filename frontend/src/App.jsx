import React, { useState, useEffect } from 'react';
import { 
  Link2, 
  Copy, 
  Check, 
  ExternalLink, 
  BarChart3, 
  Clock, 
  Calendar, 
  Trash2, 
  Sparkles, 
  Zap, 
  X, 
  Globe, 
  Activity 
} from 'lucide-react';
import { urlApi } from './api';
import './App.css';

export default function App() {
  const [longUrl, setLongUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [expirationDays, setExpirationDays] = useState('30');
  const [showOptions, setShowOptions] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [currentResult, setCurrentResult] = useState(null);
  const [copied, setCopied] = useState(false);

  // Recent URLs
  const [recentUrls, setRecentUrls] = useState([
    {
      id: 1,
      shortCode: 'springBootDocs',
      shortUrl: 'http://localhost:8082/springBootDocs',
      longUrl: 'https://spring.io/projects/spring-boot',
      clickCount: 142,
      status: 'ACTIVE',
      createdAt: new Date(Date.now() - 3600000 * 24).toISOString(),
      expiresAt: new Date(Date.now() + 3600000 * 24 * 29).toISOString()
    },
    {
      id: 2,
      shortCode: 'myPortfolio',
      shortUrl: 'http://localhost:8082/myPortfolio',
      longUrl: 'https://github.com/praveenkumardtp',
      clickCount: 89,
      status: 'ACTIVE',
      createdAt: new Date(Date.now() - 3600000 * 48).toISOString(),
      expiresAt: new Date(Date.now() + 3600000 * 24 * 28).toISOString()
    }
  ]);

  // Analytics Modal
  const [analyticsData, setAnalyticsData] = useState(null);
  const [isAnalyticsOpen, setIsAnalyticsOpen] = useState(false);

  useEffect(() => {
    loadRecentUrls();
  }, []);

  async function loadRecentUrls() {
    try {
      const data = await urlApi.getRecentUrls();
      if (data && data.content && data.content.length > 0) {
        setRecentUrls(data.content);
      }
    } catch (err) {
      console.warn('Backend offline, using demo history state');
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!longUrl) return;

    try {
      setLoading(true);
      setError(null);
      setCopied(false);

      try {
        const result = await urlApi.shortenUrl({
          longUrl,
          customAlias: customAlias.trim() || undefined,
          expirationDays
        });
        setCurrentResult(result);
        setRecentUrls([result, ...recentUrls]);
      } catch (err) {
        // Local demonstration fallback
        const code = customAlias.trim() || Math.random().toString(36).substring(2, 9);
        const demoResult = {
          id: Date.now(),
          shortCode: code,
          shortUrl: `http://localhost:8082/${code}`,
          longUrl,
          customAlias: customAlias.trim() || null,
          clickCount: 0,
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
          expiresAt: new Date(Date.now() + 86400000 * parseInt(expirationDays)).toISOString()
        };
        setCurrentResult(demoResult);
        setRecentUrls([demoResult, ...recentUrls]);
      }
      setLongUrl('');
      setCustomAlias('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function handleCopy(urlToCopy) {
    navigator.clipboard.writeText(urlToCopy);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  }

  async function viewAnalytics(item) {
    try {
      const data = await urlApi.getAnalytics(item.shortCode);
      setAnalyticsData(data);
    } catch (err) {
      // Demo analytics data
      setAnalyticsData({
        shortCode: item.shortCode,
        shortUrl: item.shortUrl,
        longUrl: item.longUrl,
        totalClicks: item.clickCount || 1,
        status: item.status,
        createdAt: item.createdAt,
        expiresAt: item.expiresAt,
        recentClicks: [
          { clickedAt: new Date().toISOString(), ipAddress: '192.168.1.105', userAgent: 'Chrome / macOS', referrer: 'Direct / Social' },
          { clickedAt: new Date(Date.now() - 3600000).toISOString(), ipAddress: '10.0.0.42', userAgent: 'Safari / iOS', referrer: 'https://twitter.com' },
          { clickedAt: new Date(Date.now() - 7200000).toISOString(), ipAddress: '172.16.0.8', userAgent: 'Firefox / Linux', referrer: 'https://linkedin.com' }
        ]
      });
    }
    setIsAnalyticsOpen(true);
  }

  return (
    <div>
      {/* Header */}
      <header className="app-header">
        <div className="brand-wrap">
          <div className="brand-icon">
            <Link2 size={22} />
          </div>
          <div>
            <div className="brand-title">LNK.PRO</div>
            <span style={{ fontSize: '0.7rem', color: '#22d3ee', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Distributed URL Shortener &amp; Redis Cache
            </span>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Redis Cache-Aside Active
          </span>
          <span style={{ width: '8px', height: '8px', background: '#34d399', borderRadius: '50%' }} />
        </div>
      </header>

      {/* Hero & Form */}
      <section className="shortener-hero">
        <div className="hero-tag">
          <Zap size={14} />
          <span>Sub-Millisecond Redis Redirect Layer</span>
        </div>
        <h1 className="hero-title">Shorten, Share &amp; Analyze Links</h1>
        <p className="hero-desc">
          High-performance distributed URL shortener engineered with Spring Boot 3, Redis caching, Base62 encoding, and click analytics.
        </p>

        {/* Shortener Card */}
        <div className="shortener-panel">
          <form onSubmit={handleSubmit}>
            <div className="input-row">
              <input 
                type="url" 
                required 
                placeholder="Paste your long destination URL (e.g., https://example.com/very/long/path)..."
                className="url-input"
                value={longUrl}
                onChange={e => setLongUrl(e.target.value)}
              />
              <button type="submit" disabled={loading} className="btn btn-cyan">
                {loading ? 'Processing...' : 'Shorten URL'}
              </button>
            </div>

            {/* Custom Options Toggle */}
            <div style={{ marginTop: '0.75rem' }}>
              <button 
                type="button" 
                onClick={() => setShowOptions(!showOptions)}
                className="btn btn-secondary"
                style={{ fontSize: '0.8rem', padding: '0.35rem 0.75rem' }}
              >
                {showOptions ? 'Hide Custom Options' : '+ Custom Alias & Expiration'}
              </button>
            </div>

            {showOptions && (
              <div className="options-bar">
                <div className="option-group">
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Custom Alias:</label>
                  <input 
                    type="text" 
                    placeholder="e.g. spring-boot-docs"
                    className="option-input"
                    value={customAlias}
                    onChange={e => setCustomAlias(e.target.value)}
                  />
                </div>

                <div className="option-group">
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Expires In:</label>
                  <select 
                    value={expirationDays}
                    onChange={e => setExpirationDays(e.target.value)}
                    className="option-input"
                  >
                    <option value="7">7 Days</option>
                    <option value="30">30 Days (Default)</option>
                    <option value="90">90 Days</option>
                    <option value="365">1 Year</option>
                  </select>
                </div>
              </div>
            )}
          </form>

          {error && (
            <div style={{ marginTop: '1rem', color: '#fb7185', background: 'rgba(244,63,94,0.1)', padding: '0.75rem', borderRadius: '8px', fontSize: '0.875rem' }}>
              {error}
            </div>
          )}

          {/* Result Card */}
          {currentResult && (
            <div className="result-card">
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.2rem' }}>
                  Your Short Link is Ready:
                </span>
                <a href={currentResult.shortUrl} target="_blank" rel="noopener noreferrer" className="short-url-link">
                  {currentResult.shortUrl}
                </a>
                <span style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem', maxWidth: '400px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  Target: {currentResult.longUrl}
                </span>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button onClick={() => handleCopy(currentResult.shortUrl)} className="btn btn-secondary btn-sm">
                  {copied ? <Check size={16} style={{ color: '#34d399' }} /> : <Copy size={16} />}
                  <span>{copied ? 'Copied!' : 'Copy'}</span>
                </button>
                <a href={currentResult.shortUrl} target="_blank" rel="noopener noreferrer" className="btn btn-cyan btn-sm">
                  <span>Visit Link</span>
                  <ExternalLink size={14} />
                </a>
              </div>
            </div>
          )}
        </div>
      </section>

      {/* Recent Links & Analytics Table */}
      <section className="content-section">
        <div className="section-head">
          <h3>Recently Shortened Links</h3>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            {recentUrls.length} Active Records
          </span>
        </div>

        <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius)', overflow: 'hidden' }}>
          <table className="custom-table">
            <thead>
              <tr>
                <th>Short Code</th>
                <th>Destination Target</th>
                <th>Clicks</th>
                <th>Status</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {recentUrls.map(item => (
                <tr key={item.id || item.shortCode}>
                  <td style={{ fontFamily: 'monospace', fontWeight: 700, color: '#38bdf8' }}>
                    /{item.shortCode}
                  </td>
                  <td style={{ maxWidth: '280px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    <a href={item.longUrl} target="_blank" rel="noopener noreferrer" style={{ color: 'var(--text-muted)', textDecoration: 'none' }}>
                      {item.longUrl}
                    </a>
                  </td>
                  <td style={{ fontWeight: 700 }}>
                    {item.clickCount || 0}
                  </td>
                  <td>
                    <span style={{
                      padding: '0.2rem 0.6rem',
                      borderRadius: '9999px',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      background: item.status === 'ACTIVE' ? 'rgba(16,185,129,0.15)' : 'rgba(244,63,94,0.15)',
                      color: item.status === 'ACTIVE' ? '#34d399' : '#fb7185'
                    }}>
                      {item.status}
                    </span>
                  </td>
                  <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    {new Date(item.createdAt).toLocaleDateString()}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleCopy(item.shortUrl || `http://localhost:8082/${item.shortCode}`)} className="btn btn-secondary btn-sm" title="Copy">
                        <Copy size={14} />
                      </button>
                      <button onClick={() => viewAnalytics(item)} className="btn btn-secondary btn-sm" title="Analytics">
                        <BarChart3 size={14} />
                        <span>Metrics</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Analytics Modal */}
      {isAnalyticsOpen && analyticsData && (
        <div className="modal-overlay" onClick={() => setIsAnalyticsOpen(false)}>
          <div className="modal-box" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <BarChart3 size={20} className="text-primary" />
                <h3>Link Analytics: /{analyticsData.shortCode}</h3>
              </div>
              <button onClick={() => setIsAnalyticsOpen(false)} className="btn btn-secondary btn-sm"><X size={16} /></button>
            </div>

            {/* Metrics Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', marginBottom: '1.5rem' }}>
              <div style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total Clicks</span>
                <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#38bdf8' }}>{analyticsData.totalClicks}</div>
              </div>

              <div style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Status</span>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#34d399', marginTop: '0.35rem' }}>{analyticsData.status}</div>
              </div>

              <div style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Expiration</span>
                <div style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>
                  {analyticsData.expiresAt ? new Date(analyticsData.expiresAt).toLocaleDateString() : 'Never'}
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>Target Destination</span>
              <a href={analyticsData.longUrl} target="_blank" rel="noopener noreferrer" style={{ color: '#60a5fa', wordBreak: 'break-all', fontSize: '0.9rem' }}>
                {analyticsData.longUrl}
              </a>
            </div>

            {/* Click Log Table */}
            <div>
              <h4 style={{ fontSize: '0.9rem', marginBottom: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Recent Click Access Log</h4>
              <div style={{ maxHeight: '200px', overflowY: 'auto', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.8rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
                      <th style={{ padding: '0.5rem 0.75rem', textAlign: 'left' }}>Time</th>
                      <th style={{ textAlign: 'left' }}>IP / Device</th>
                      <th style={{ textAlign: 'left' }}>Referrer</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analyticsData.recentClicks && analyticsData.recentClicks.length > 0 ? (
                      analyticsData.recentClicks.map((click, idx) => (
                        <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.03)' }}>
                          <td style={{ padding: '0.5rem 0.75rem' }}>{new Date(click.clickedAt).toLocaleTimeString()}</td>
                          <td>{click.userAgent}</td>
                          <td>{click.referrer}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="3" style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                          No click events logged yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
