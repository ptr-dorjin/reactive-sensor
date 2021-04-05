import React from 'react';
import SensorData from "../domain/SensorData";
import MessageService from "../services/MessageService";
import TemperatureChart from "./TemperatureChart";

interface IProps {
}

interface IState {
    connected: boolean
    error: any,
    locations: Set<string>,
    // Used for Chart.js labels
    chartLabels: Date[],
    // Chart.js limitation: size of data inside of each dataset should be = size of labels array
    chartDatasets: any[]
}

const LIMIT_TIME_STAMPS = 100

class SensorDataComponent extends React.Component<IProps, IState> {
    service: MessageService;

    constructor(props: IProps) {
        super(props);
        this.state = {
            connected: false,
            error: null,
            locations: new Set(),
            chartLabels: [],
            chartDatasets: [],
        }
        this.service = new MessageService(
            this.onConnect.bind(this),
            this.onNextSensorData.bind(this)
        )
    }

    onConnect(error: any) {
        this.setState({
            connected: !error,
            error,
        });
    }

    onNextSensorData(error: any, sensorData: SensorData) {
        if (error) {
            console.error(error);
            this.setState({
                error,
            });
        } else {
            sensorData.instant = new Date(sensorData.instant)
            console.log("Received", sensorData)
            this.appendToState(sensorData)
        }
    }

    public appendToState(sensorData: SensorData) {
        const location = sensorData.location;
        const locations = this.state.locations;
        const labels = this.state.chartLabels;
        const datasets: any[] = this.state.chartDatasets;

        if (!locations.has(location)) {
            // new location that we haven't seen before: populate old timestamps with null to maintain the size of arrays
            let newLocationData = Array<number | null>();
            locations.add(location)
            labels.forEach(() => newLocationData.push(null))
            datasets.push({
                label: location,
                data: newLocationData,
                spanGaps: true,
                fill: false,
                borderColor: SensorDataComponent.getRandomColor()
            })
        }

        labels.push(sensorData.instant)
        datasets.forEach(dataset => {
            if (dataset.label === location)
                dataset.data.push(sensorData.temperature)
            else dataset.data.push(null)
            dataset.data = dataset.data.slice(-LIMIT_TIME_STAMPS)
        })

        this.setState({
            locations: locations,
            chartLabels: labels.slice(-LIMIT_TIME_STAMPS),
            chartDatasets: datasets.slice(-LIMIT_TIME_STAMPS),
        })
    }

    private static getRandomColor() {
        let rgb = [];
        for (let i = 0; i < 3; i++) {
            rgb.push(Math.round(Math.random() * 255));
        }
        let [r, g, b] = rgb;
        return `rgba(${r}, ${g}, ${b}, 0.2)`;
    }

    componentDidMount() {
        this.service.connect();
    }

    componentWillUnmount() {
        this.service.close();
    }

    render() {
        return (
            <div className="row">
                <TemperatureChart labels={this.state.chartLabels} datasets={this.state.chartDatasets}/>
            </div>
        );
    }
}

export default SensorDataComponent;
