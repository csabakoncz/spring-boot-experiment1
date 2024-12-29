import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BASE_PATH, Customer, HelloWorldControllerService } from '../api';
import { lastValueFrom } from 'rxjs';

describe('Test HelloWorldControllerService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        HelloWorldControllerService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: BASE_PATH,
          useValue: '/api',
        },
      ],
    });
  });

  it('should create the service', async () => {
    const service = TestBed.inject(HelloWorldControllerService);
    const httpTesting = TestBed.inject(HttpTestingController);
    expect(service).toBeTruthy();

    const greetingsPromise = lastValueFrom(service.sayHello('csaba'));

    const req = httpTesting.expectOne(
      '/api/hello-world?name=csaba',
      'Request to load the customersList'
    );

    // We can assert various properties of the request if desired.
    expect(req.request.method).toBe('GET');

    // Flushing the request causes it to complete, delivering the result.
    const response = [customer({ id: 10 }), customer({ id: 20 })];
    req.flush(response);

    const greetings = await greetingsPromise;
    for (const g of greetings) {
      console.log(`person: ${g.lastName}, ${g.firstName}`);
    }

    // Finally, we can assert that no other requests were made.
    httpTesting.verify();
  });
});

function customer(info: { id: number }): Customer {
  return {
    id: info.id,
    firstName: `First${info.id}`,
    lastName: `Last${info.id}`,
  };
}
