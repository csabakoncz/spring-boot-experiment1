import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { HelloWorldControllerService } from '../api';
import { of } from 'rxjs';

describe('AppComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: HelloWorldControllerService,
          useValue: jasmine.createSpyObj('HelloWorldControllerService', [
            'sayHello',
          ]),
        },
      ],
    });
  });

  it('should create the app', async () => {
    const helloService = TestBed.inject(HelloWorldControllerService);

    (helloService.sayHello as jasmine.Spy).and.returnValue(
      of(Promise.resolve([{ id: 10 }]))
    );

    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
    // fixture.autoDetectChanges()
    // either autodetection or this call is needed to have the initial state rendered:
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.app-error')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.app-loading')).toBeTruthy();

    // wait for promises to settle:
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.app-error')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.app-loading')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.app-customers li')).toHaveSize(
      1
    );
  });

  it(`should display error`, async () => {
    const helloService = TestBed.inject(HelloWorldControllerService);

    (helloService.sayHello as jasmine.Spy).and.returnValue(
      of(Promise.reject({ msg: 'oops' }))
    );

    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
    // fixture.autoDetectChanges()
    // either autodetection or this call is needed to have the initial state rendered:
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.app-error')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.app-loading')).toBeTruthy();

    // wait for promises to settle:
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.app-error')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.app-loading')).toBeFalsy();
    expect(
      fixture.nativeElement.querySelector('.app-customers li')
    ).toBeFalsy();
  });
});
