import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import './App.css';

const STARTER_CODE = {
  python: `# Python Dynamic Pricing Engine
data = {"basePrice": 100, "demandFactor": 1.5}
final_price = data["basePrice"] * data["demandFactor"]

print("--- PolyglotMesh Pricing Algorithm ---")
print(f"Base Price    : $\${data['basePrice']}")
print(f"Demand Factor : {data['demandFactor']}")
print(f"Final Price   : $\${final_price}")
`,
  javascript: `// JavaScript Tier Validation Logic
const price = 150;
const tier = price > 100 ? "Premium Tier" : "Standard Tier";

console.log("--- PolyglotMesh Tier Validator ---");
console.log("Calculated Price : $" + price);
console.log("Assigned Tier    : " + tier);
`
};

export default function App() {
  const [activeTab, setActiveTab] = useState('python');
  const [codeState, setCodeState] = useState(STARTER_CODE);
  const [consoleOutput, setConsoleOutput] = useState({
    status: 'idle',
    message: 'Click "▶ Run" to execute code natively inside GraalVM.',
    timeMs: null
  });
  const [loading, setLoading] = useState(false);

  const handleEditorChange = (value) => {
    setCodeState((prev) => ({
      ...prev,
      [activeTab]: value || ''
    }));
  };

  const handleRun = async () => {
    setLoading(true);
    setConsoleOutput({
      status: 'running',
      message: 'Submitting code to GraalVM sandbox...',
      timeMs: null
    });

    const currentCode = codeState[activeTab];
    const languagePayload = activeTab === 'javascript' ? 'js' : 'python';

    try {
      const response = await fetch('http://localhost:8080/api/execute', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          language: languagePayload,
          code: currentCode
        })
      });

      if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}`);
      }

      const result = await response.json();

      if (result.success) {
        setConsoleOutput({
          status: 'success',
          message: result.output || '(Execution finished with no output)',
          timeMs: result.executionTimeMs
        });
      } else {
        setConsoleOutput({
          status: 'error',
          message: result.error || 'Execution failed',
          timeMs: result.executionTimeMs
        });
      }
    } catch (err) {
      setConsoleOutput({
        status: 'error',
        message: `[Connection Info] Backend not yet connected at http://localhost:8080\n\nWhen Meemansa runs her Spring Boot backend, this Run button will execute live in your GraalVM engine!`,
        timeMs: 0
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ide-container">
      <header className="ide-header">
        <div className="ide-title">
          <span>🚀 PolyglotMesh</span>
          <span className="badge">GraalVM In-Memory Runtime</span>
        </div>
        <div style={{ fontSize: '12px', color: '#858585' }}>
          Zero Network Overhead Sandbox
        </div>
      </header>

      <div className="toolbar">
        <div className="tabs">
          <button
            className={`tab-btn ${activeTab === 'python' ? 'active' : ''}`}
            onClick={() => setActiveTab('python')}
          >
            🐍 Python
          </button>
          <button
            className={`tab-btn ${activeTab === 'javascript' ? 'active' : ''}`}
            onClick={() => setActiveTab('javascript')}
          >
            ⚡ JavaScript
          </button>
        </div>

        <button className="run-btn" onClick={handleRun} disabled={loading}>
          {loading ? '⏳ Running...' : '▶ Run'}
        </button>
      </div>

      <div className="workspace">
        <div className="editor-pane">
          <Editor
            height="100%"
            language={activeTab}
            value={codeState[activeTab]}
            onChange={handleEditorChange}
            theme="vs-dark"
            options={{
              fontSize: 14,
              minimap: { enabled: false },
              scrollBeyondLastLine: false,
              automaticLayout: true,
              tabSize: 4
            }}
          />
        </div>

        <div className="console-pane">
          <div className="console-header">
            <span>Terminal Output</span>
            {consoleOutput.timeMs !== null && (
              <span className="metric-pill">⏱ {consoleOutput.timeMs} ms</span>
            )}
          </div>
          <div className="console-body">
            {consoleOutput.status === 'idle' && (
              <span className="console-info">{consoleOutput.message}</span>
            )}
            {consoleOutput.status === 'running' && (
              <span className="console-info">{consoleOutput.message}</span>
            )}
            {consoleOutput.status === 'success' && (
              <span className="console-success">{consoleOutput.message}</span>
            )}
            {consoleOutput.status === 'error' && (
              <span className="console-error">{consoleOutput.message}</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}