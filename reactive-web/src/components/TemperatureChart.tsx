import React from 'react';
import {Line} from "react-chartjs-2";

interface IProps {
    labels: string[],
    datasets: any[]
}

let options = {
    animation: false,
    scales: {
        xAxes: [
            {
                ticks: {
                    autoSkipPadding: 50,
                    maxRotation: 0
                }
            }
        ]
    }
};

class TemperatureChart extends React.Component<IProps, {}> {

    render() {
        return <Line data={{
            labels: this.props.labels,
            datasets: this.props.datasets
        }} options={options} redraw={true}/>
    }
}

export default TemperatureChart
