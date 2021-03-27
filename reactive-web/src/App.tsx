import SensorDataComponent from "./components/SensorDataComponent";
import React from "react";

export function App() {
    return (
        <div className="container">
            <h1>Reactive web app sample</h1>
            <div className="row">
                <SensorDataComponent/>
            </div>
        </div>
    )
}

export default App;
