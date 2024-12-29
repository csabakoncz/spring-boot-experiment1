import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Customer, HelloWorldControllerService } from '../api';
import { lastValueFrom } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'frontend';
  loading = true;
  error?: string;
  customers?: Customer[];

  constructor(private helloService: HelloWorldControllerService) {
    lastValueFrom(helloService.sayHello())
      .then((customers) => {
        this.customers = customers;
      })
      .catch((reason) => {
        this.error = 'ERROR: ' + reason;
      })
      .finally(() => {
        this.loading = false;
      });
  }
}
