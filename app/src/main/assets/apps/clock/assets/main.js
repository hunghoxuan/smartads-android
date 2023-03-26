 /*
            var dialLines = document.getElementsByClassName('diallines');
            var clockEl = document.getElementsByClassName('clock')[0];

            for (var i = 1; i < 60; i++) {
              clockEl.innerHTML += "<div class='diallines'></div>";
              dialLines[i].style.transform = "rotate(" + 6 * i + "deg)";
            }

            function clock() {
              var weekday = [
                    "Sunday",
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday"
                  ],
                  d = new Date(),
                  h = d.getHours(),
                  m = d.getMinutes(),
                  s = d.getSeconds(),
                  date = d.getDate(),
                  month = d.getMonth() + 1,
                  year = d.getFullYear(),

                  hDeg = h * 30 + m * (360/720),
                  mDeg = m * 6 + s * (360/3600),
                  sDeg = s * 6,

                  hEl = document.querySelector('.hour-hand'),
                  mEl = document.querySelector('.minute-hand'),
                  sEl = document.querySelector('.second-hand'),
                  dateEl = document.querySelector('.date'),
                  dayEl = document.querySelector('.day');

                  var day = weekday[d.getDay()];

              if(month < 9) {
                month = "0" + month;
              }

              hEl.style.transform = "rotate("+hDeg+"deg)";
              mEl.style.transform = "rotate("+mDeg+"deg)";
              sEl.style.transform = "rotate("+sDeg+"deg)";
              dateEl.innerHTML = date+"/"+month+"/"+year;
              dayEl.innerHTML = day;
            }

            window.onload=function() {
                clock();
                setInterval("clock()", 100);
            }
            */

/*
            var s = Snap(document.getElementById("clock"));

		var seconds = s.select("#seconds"),
		    minutes = s.select("#minutes"),
		    hours   = s.select("#hours"),
		    rim     = s.select("#rim"),
		    face    = {
		      elem: s.select("#face"),
		      cx: s.select("#face").getBBox().cx,
		      cy: s.select("#face").getBBox().cy,
		    },
		    angle   = 0,
		    easing = function(a) {
		      return a==!!a?a:Math.pow(4,-10*a)*Math.sin((a-.075)*2*Math.PI/.3)+1;
		    };

		var sshadow = seconds.clone(),
			mshadow = minutes.clone(),
			hshadow = hours.clone(),
			rshadow = rim.clone(),
			shadows = [sshadow, mshadow, hshadow];

		//Insert shadows before their respective opaque pals
		seconds.before(sshadow);
		minutes.before(mshadow);
		hours.before(hshadow);
		rim.before(rshadow);

		//Create a filter to make a blurry black version of a thing
		var filter = Snap.filter.blur(0.1) + Snap.filter.brightness(0);

		//Add the filter, shift and opacity to each of the shadows
		shadows.forEach(function(el){
			el.attr({
				transform: "translate(0, 2)",
				opacity: 0.2,
				filter: s.filter(filter)
			});
		})

		rshadow.attr({
			transform: "translate(0, 8) ",
      opacity: 0.5,
			filter: s.filter(Snap.filter.blur(0, 8)+Snap.filter.brightness(0)),
		})

		function update() {
		  var time = new Date();
		  setHours(time);
		  setMinutes(time);
		  setSeconds(time);
		}

		function setHours(t) {
		  var hour = t.getHours();
		  hour %= 12;
		  hour += Math.floor(t.getMinutes()/10)/6;
		  var angle = hour*360/12;
		  hours.animate(
		    {transform: "rotate("+angle+" 244 251)"},
		    100,
		    mina.linear,
		    function(){
		      if (angle === 360){
		        hours.attr({transform: "rotate("+0+" "+face.cx+" "+face.cy+")"});
		        hshadow.attr({transform: "translate(0, 2) rotate("+0+" "+face.cx+" "+face.cy+2+")"});
		      }
		    }
		  );
		  hshadow.animate(
		    {transform: "translate(0, 2) rotate("+angle+" "+face.cx+" "+face.cy+2+")"},
		    100,
		    mina.linear
		  );
		}
		function setMinutes(t) {
		  var minute = t.getMinutes();
		  minute %= 60;
		  minute += Math.floor(t.getSeconds()/10)/6;
		  var angle = minute*360/60;
		  minutes.animate(
		    {transform: "rotate("+angle+" "+face.cx+" "+face.cy+")"},
		    100,
		    mina.linear,
		    function(){
		      if (angle === 360){
		        minutes.attr({transform: "rotate("+0+" "+face.cx+" "+face.cy+")"});
		        mshadow.attr({transform: "translate(0, 2) rotate("+0+" "+face.cx+" "+face.cy+2+")"});
		      }
		    }
		  );
		  mshadow.animate(
		    {transform: "translate(0, 2) rotate("+angle+" "+face.cx+" "+face.cy+2+")"},
		    100,
		    mina.linear
		  );
		}
		function setSeconds(t) {
		  t = t.getSeconds();
		  t %= 60;
		  var angle = t*360/60;
		  //if ticking over to 0 seconds, animate angle to 360 and then switch angle to 0
		  if (angle === 0) angle = 360;
		  seconds.animate(
		    {transform: "rotate("+angle+" "+face.cx+" "+face.cy+")"},
		    600,
		    easing,
		    function(){
		      if (angle === 360){
		        seconds.attr({transform: "rotate("+0+" "+face.cx+" "+face.cy+")"});
		        sshadow.attr({transform: "translate(0, 2) rotate("+0+" "+face.cx+" "+face.cy+2+")"});
		      }
		    }
		  );
		  sshadow.animate(
		    {transform: "translate(0, 2) rotate("+angle+" "+face.cx+" "+face.cy+2+")"},
		    600,
		    easing
		  );
		}
		setInterval(update, 1000);
		*/

         function showTime(){
            var weekday = [
                    "Sunday",
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday"
                  ];

            var date = new Date();
            var h = date.getHours(); // 0 - 23
            var m = date.getMinutes(); // 0 - 59
            var s = date.getSeconds(); // 0 - 59
            var session = "AM";
            var date1 = date.getDate();
            var month = date.getMonth() + 1;
            var year = date.getFullYear();
             if(month < 9) {
                month = "0" + month;
              }

            if(h == 0){
                h = 12;
            }

            if(h > 12){
                h = h - 12;
                session = "PM";
            }

            h = (h < 10) ? "0" + h : h;
            m = (m < 10) ? "0" + m : m;
            s = (s < 10) ? "0" + s : s;

            var time = h + ":" + m + ":" + s + " " + session + "<br/><div style='font-size:50% !important'>" + date1 + "/" + month + "/" + year + "</div>";
            document.getElementById("MyClockDisplay").innerHTML = time;
            setTimeout(showTime, 1000);

        }