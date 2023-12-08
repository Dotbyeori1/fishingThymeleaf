// 달력 <--> 리스트
function changeTab(selectedTab) {
    // 모든 탭을 숨김
    let tabs = document.getElementsByClassName('tab');
    for (let i = 0; i < tabs.length; i++) {
        tabs[i].style.display = 'none';
    }

    // 선택한 탭만 보여줌
    document.getElementById(selectedTab).style.display = 'block';
}

// Date 객체 생성
let date = new Date();
let dayNames = ["일", "월", "화", "수", "목", "금", "토"];

// 날짜별로 예약이 얼마나 찼는지와 state를 계산
const reservationCountByDate = {};
reservations.forEach(reservation => {
    const date = reservation.regDate;
    const state = reservation.state;
    const memberCount = reservation.member;
    const regName = reservation.regName;
    if (!reservationCountByDate[date]) {
        reservationCountByDate[date] = {
            totalMember: 0,
            confirmedMember: 0,
            regInfo: [] // regName과 memberCount를 합쳐 저장할 배열
        };
    }

    reservationCountByDate[date].totalMember += reservation.member;

    if (state) { // 확정된 경우만
        reservationCountByDate[date].confirmedMember += reservation.member;
        // regName과 memberCount를 합쳐서 저장
        reservationCountByDate[date].regInfo.push(` ${regName}님(${memberCount}명)`);
    }
});

// regDate 별로 available 여부와 message를 저장하는 객체
const availabilityByDate = {};
const messageByDate = {};
const fishingSortBydate = {};
const extraMembersBydate = {};
reservationDates.forEach(reservationDate => {
    const regDate = reservationDate.regDate;
    const available = reservationDate.available;
    const message = reservationDate.message;
    const fishingSort = reservationDate.fishingSort;
    const extraMembers = reservationDate.extrasMembers;
    availabilityByDate[regDate] = available;
    messageByDate[regDate] = message;
    fishingSortBydate[regDate] = fishingSort;
    extraMembersBydate[regDate] = extraMembers;
});

function redirectToReservation(buttonElement) {
    const fullDate = buttonElement.getAttribute('data-full-date');
    let extras = extraMembersBydate[fullDate] || 0; // 예약 가능한 최대 인원
    let confirmedMemberCount = reservationCountByDate[fullDate]?.confirmedMember || 0;
    let extraMembers = extras - confirmedMemberCount;
    let isAvailable = (extraMembers > 0);
    let isAvailable2 = availabilityByDate[fullDate]
    let btnText3 = isAvailable ? 'true' : 'false'
    if (isAvailable2) {
        if(isAvailable) {
            window.location.href = "/reservation/register?mainYN=true&regDate=" + fullDate + "&registerYN=" + btnText3;
        }
    } else {
        return;
    }
}

const renderCalendar = () => {
    const viewYear = date.getFullYear();
    const viewMonth = date.getMonth();
    // 현재 날짜 가져오기
    const today = new Date();
    // 시간이 16시 이후인지 확인
    // if (today.getHours() >= 10) {
    //     today.setDate(today.getDate() + 1);  // 다음 날짜로 변경
    // }
    today.setHours(0, 0, 0, 0);  // 시간, 분, 초, 밀리초를 0으로 설정하여 날짜만 비교

    // year-month 채우기
    // document.querySelector('.year-month').textContent = `${viewYear}년 ${viewMonth + 1}월`;

    // 지난 달 마지막 Date, 이번 달 마지막 Date
    const prevLast = new Date(viewYear, viewMonth, 0);
    const thisLast = new Date(viewYear, viewMonth + 1, 0);

    const PLDate = prevLast.getDate();
    const PLDay = prevLast.getDay();

    const TLDate = thisLast.getDate();
    const TLDay = thisLast.getDay();


    // Dates 기본 배열들
    const prevDates = [];
    const thisDates = [...Array(TLDate + 1).keys()].slice(1);
    const nextDates = [];

    // prevDates 계산
    if (PLDay !== 6) {
        for (let i = 0; i < PLDay + 1; i++) {
            prevDates.unshift(PLDate - i);
        }
    }

    // nextDates 계산
    for (let i = 1; i < 7 - TLDay; i++) {
        nextDates.push(i)
    }

    // Dates 합치기
    const dates = prevDates.concat(thisDates, nextDates);
    const dates2 = prevDates.concat(thisDates, nextDates);

    function updateBtnText(isAvailable2) {
        const stateBtn = document.querySelector('.detailBtnClass');  // id가 아닌 class로 선택
        const btnText = stateBtn.getAttribute('data-btn-text');
        const btnText2 = stateBtn.getAttribute('data-btn-text2');
        if (isAvailable2) {
            stateBtn.innerText = btnText;
        } else {
            stateBtn.innerText = btnText2;
        }
    }

    // 클릭 이벤트 함수
    function addClickEventToSelects(selectElements) {
        selectElements.forEach((select) => {
            select.addEventListener("click", function () {
                // 먼저, 모든 요소의 배경색을 초기화합니다.
                selectElements.forEach((elem) => {
                    elem.style.backgroundColor = "";
                });

                // 클릭된 요소의 배경색만 변경합니다.
                this.style.backgroundColor = "#c0f2ff";
            });
        });
    }


    // showDetails 함수를 외부에 정의
    function showDetails(fullDate, fullDate2, dayOfWeek, btnText,
                         btnText2, extraMembers, btnClass2, btnClass, message, regInfo, fishingSort) {

        const linkdateElements = document.querySelectorAll('.linkDate');
        linkdateElements.forEach(element => {
            element.innerText = fullDate2;
            element.setAttribute('data-full-date', fullDate2);
        });

        const detailDateElements = document.querySelectorAll('.detailDate');
        detailDateElements.forEach(element => {
            element.innerText = fullDate;
        });

        const detailDayOfWeekElements = document.querySelectorAll('.detailDayOfWeek');
        detailDayOfWeekElements.forEach(element => {
            element.innerText = dayNames[dayOfWeek];
        });

        const detailExtraMembersElements = document.querySelectorAll('.detailExtraMembers');
        detailExtraMembersElements.forEach(element => {
            element.innerText = `${extraMembers}명`;
        });

        const dataMessageElements = document.querySelectorAll('.dataMessage');
        dataMessageElements.forEach(element => {
            element.innerText = `${message}`;
        });

        const dataFishingSortElements = document.querySelectorAll('.dataFishingSort');
        dataFishingSortElements.forEach(element => {
            element.innerText = `${fishingSort}`;
        });

        const dataRegInfoElements = document.querySelectorAll('.dataRegInfo');
        dataRegInfoElements.forEach(element => {
            if (regInfo != null) {
                const regInfoStr = regInfo.replace(/\,/g, '<br/>');
                element.innerHTML = regInfoStr;
            } else {
                element.innerText = regInfo || "";
            }
        });

        let isAvailable2 = availabilityByDate[fullDate]
        const stateBtnElements = document.querySelectorAll('.detailBtnClass');
        stateBtnElements.forEach(element => {
            if (isAvailable2) {
                element.innerText = btnText2;
                element.setAttribute('data-btn-class2', btnClass2);
                element.setAttribute('data-btn-text2', btnText2);
                element.className = `btn ${btnClass2} detailBtnClass linkDate`;
                updateBtnText(false);
            } else {
                element.innerText = btnText;
                element.setAttribute('data-btn-class', btnClass);
                element.setAttribute('data-btn-text', btnText);
                element.className = `btn ${btnClass} detailBtnClass linkDate`;
                updateBtnText(true);
            }
        });
    }

    // 클릭시 상세보기가 되도록 함
    function handleDateClick(event) {
        const dateElement1 = event.target.closest('.date');
        const dateElement2 = event.target.closest('.date2');
        const dateElement = dateElement1 || dateElement2;

        if (dateElement && dateElement.getAttribute('data-this-month') === "true") {
            const fullDate = dateElement.getAttribute('data-full-date');
            const fullDate2 = dateElement.getAttribute('data-full-date');
            const dayOfWeek = dateElement.getAttribute('data-day-of-week');
            const btnText = dateElement.getAttribute('data-btn-text');
            const btnText2 = dateElement.getAttribute('data-btn-text2');
            const extraMembers = dateElement.getAttribute('data-extra-members');
            const btnClass2 = dateElement.getAttribute('data-btn-class2');
            const btnClass = dateElement.getAttribute('data-btn-class');
            const message = dateElement.getAttribute('data-message');
            const regInfo = dateElement.getAttribute('data-regInfo');
            const fishingSort = dateElement.getAttribute('data-fishingSort')

            showDetails(fullDate, fullDate2, dayOfWeek, btnText, btnText2,
                extraMembers, btnClass2, btnClass, message, regInfo, fishingSort);
        }
    }

    document.querySelector('.dates').addEventListener('click', handleDateClick);
    document.querySelector('.dates2').addEventListener('click', handleDateClick);


    function getTomorrowDate() {
        today.setDate(today.getDate());
        return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    }

    window.onload = function () {
        const tomorrowDate = getTomorrowDate();
        const tomorrowElement = document.querySelector(`[data-full-date="${tomorrowDate}"]`);

        if (tomorrowElement) { // 내일 날짜에 해당하는 DOM 요소가 있다면
            tomorrowElement.style.backgroundColor = "#c0f2ff";
            const fullDate = tomorrowElement.getAttribute('data-full-date');
            const fullDate2 = tomorrowElement.getAttribute('data-full-date');
            const dayOfWeek = tomorrowElement.getAttribute('data-day-of-week');
            const btnText = tomorrowElement.getAttribute('data-btn-text');
            const btnText2 = tomorrowElement.getAttribute('data-btn-text2');
            const extraMembers = tomorrowElement.getAttribute('data-extra-members');
            const btnClass2 = tomorrowElement.getAttribute('data-btn-class2');
            const btnClass = tomorrowElement.getAttribute('data-btn-class');
            const message = tomorrowElement.getAttribute('data-message');
            const regInfo = tomorrowElement.getAttribute('data-regInfo');
            const fishingSort = tomorrowElement.getAttribute('data-fishingSort')
            showDetails(fullDate, fullDate2, dayOfWeek, btnText, btnText2,
                extraMembers, btnClass2, btnClass, message, regInfo, fishingSort);
        }

    };

    // Dates 정리
    dates.forEach((currentDate, i) => {
        const paddedMonth = String(viewMonth + 1).padStart(2, '0');
        const paddedDate = String(currentDate).padStart(2, '0');
        const fullDate = `${viewYear}-${paddedMonth}-${paddedDate}`;
        let dayOfWeek = new Date(viewYear, viewMonth, currentDate).getDay();
        let message = messageByDate[fullDate] || '';
        let fishingSort = fishingSortBydate[fullDate] || '';
        let confirmedMemberCount = reservationCountByDate[fullDate]?.confirmedMember || 0;
        let regInfo = reservationCountByDate[fullDate]?.regInfo || '';
        let dateOfFullDate = new Date(fullDate);
        let isAvailable2 = availabilityByDate[fullDate] && dateOfFullDate  >= today;  // true 일 경우에만 예약 가능
        let extras = extraMembersBydate[fullDate] || 0; // 예약 가능한 최대 인원
        let extraMembers = extras - confirmedMemberCount; // 확정된 인원을 빼서 남은 인원을 계산
        let isAvailable = (extraMembers > 0);
        let btnClass = isAvailable2 ? 'btn-success' : 'btn-danger';
        let btnClass2 = isAvailable ? 'btn-success' : 'btn-warning';
        let btnText = isAvailable2 ? '예약가능' : '예약불가';
        let btnText2 = isAvailable ? btnText : '예약완료';
        let isThisMonth = i >= prevDates.length && i < (prevDates.length + thisDates.length);

        if (isThisMonth) {
            dates[i] = `
             <div class="date select" data-full-date="${fullDate}" data-day-of-week="${dayOfWeek}" data-btn-text2="${btnText2}"
             data-btn-text="${btnText}" data-extra-members="${extraMembers}" data-this-month="true" data-regInfo="${regInfo}"
             data-btn-class2="${btnClass2}" data-btn-class="${btnClass}" data-message="${message}" data-fishingsort="${fishingSort}">
                  <div class="${isThisMonth ? 'this-month' : 'other-month'}">
                    ${currentDate}
                    <div style="margin-bottom: 5px;">
                        <span class="btn-light btn-sm">종류</span><br/>
                        <span style="margin-top: 2px; font-size: 14px; color: black;">${fishingSort}</span></div>
                    <div>
                        <span class="rounded-pill ${isAvailable2? btnClass2 : 'btn-danger'}" style="padding: 3px;">
                            ${isAvailable2? btnText2 : '예약불가'}
                        </span>
                    </div>
                 </div>
            </div>
         `;
        } else {
            dates[i] = `
                     <div class="date" data-this-month="false">
                     </div>
                 `;
        }
    });

    // Dates 그리기
    document.querySelector('.dates').innerHTML = dates.join('');

    const selects1 = document.querySelectorAll('.dates .select');
    addClickEventToSelects(selects1);

    dates2.forEach((currentDate, i) => {
        const paddedMonth = String(viewMonth + 1).padStart(2, '0');
        const paddedDate = String(currentDate).padStart(2, '0');
        const fullDate = `${viewYear}-${paddedMonth}-${paddedDate}`;
        let dayOfWeek = new Date(viewYear, viewMonth, currentDate).getDay();
        let message = messageByDate[fullDate] || '';
        let fishingSort = fishingSortBydate[fullDate] || '';
        let confirmedMemberCount = reservationCountByDate[fullDate]?.confirmedMember || 0;
        let regInfoArray = reservationCountByDate[fullDate]?.regInfo || '';
        let dateOfFullDate = new Date(fullDate);
        let isAvailable2 = availabilityByDate[fullDate]; // true 일 경우에만 예약 가능
        let extras = extraMembersBydate[fullDate] || 0; // 예약 가능한 최대 인원
        let extraMembers = extras - confirmedMemberCount; // 확정된 인원을 빼서 남은 인원을 계산
        let isAvailable = (extraMembers > 0);
        let btnClass = isAvailable2 ? 'btn-success' : 'btn-danger';
        let btnClass2 = isAvailable ? 'btn-success' : 'btn-warning';
        let btnText = isAvailable2 ? '예약가능' : '예약불가';
        let btnText2 = isAvailable ? btnText : '예약완료';
        let isThisMonth = i >= prevDates.length && i < (prevDates.length + thisDates.length);
        const regInfo = Array.isArray(regInfoArray) ? regInfoArray.join('<br/>') : '';        // fullDate를 Date 객체로 변환


        let buttonAttributes = '';
        if (isAvailable2 && isAvailable) {
            buttonAttributes = `onclick='window.location.href = "/reservation/register?mainYN=true&regDate=${fullDate}"'`;
        }

        if (isThisMonth && (dateOfFullDate >= today)) {
            dates2[i] = `
            <tr class="date select" data-full-date="${fullDate}" data-day-of-week="${dayOfWeek}" data-btn-text2="${btnText2}"
            data-btn-text="${btnText}" data-extra-members="${extraMembers}" data-this-month="true" data-regInfo="${regInfo}"
            data-btn-class2="${btnClass2}" data-btn-class="${btnClass}" data-message="${message}" data-fishingsort="${fishingSort}">
                <td class="col">${fullDate}(${dayNames[dayOfWeek]})</td>
                <td>
                    <button class="btn ${isAvailable2 ? btnClass2 : btnClass}" ${buttonAttributes} style="width: 100%; height: 100%; text-align: center;">
                    ${isAvailable2 ? btnText2 : btnText}
                    </button>
                </td>
                <td class="override-color"><div style="margin-bottom: 5px;"><span class="btn-light btn-sm">종류</span>&nbsp;<span style="margin-top: 2px; font-size: 14px">${fishingSort}</span></div><div>${regInfo}</div></td>
                <td class="override-color">${extraMembers}명</td>
            </tr>
        `;
        } else {
            dates2[i] = `
            <tr class="date" data-this-month="false">
            </tr>
        `;
        }
    });

    document.querySelector('.dates2').innerHTML = dates2.join('');

    const selects2 = document.querySelectorAll('.dates2 .select');
    addClickEventToSelects(selects2);
}


renderCalendar();

document.addEventListener("DOMContentLoaded", function () {
    const today = new Date();

    const currentMonth = today.getMonth();
    const currentYear = today.getFullYear();
    const monthSelector = document.getElementById("monthSelector");

    const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
    const options = [];

    for (let i = 0; i < 3; i++) {
        const monthIndex = (currentMonth + i) % 12;
        const yearOffset = Math.floor((currentMonth + i) / 12);
        const year = currentYear + yearOffset;
        const monthName = monthNames[monthIndex];
        options.push(` <option value = "${year}-${monthIndex}">${year}년 ${monthName} </option>`);
    }

    monthSelector.innerHTML = options.join('');

    monthSelector.addEventListener("change", function () {
        const [selectedYear, selectedMonth] = this.value.split('-').map(Number);
        const originalDate = date.getDate();  // 원래의 일자 저장
        date.setDate(1);  // 일자를 일시적으로 1일로 설정
        date.setFullYear(selectedYear);
        date.setMonth(selectedMonth);

        const lastDayOfMonth = new Date(selectedYear, selectedMonth + 1, 0).getDate(); // 해당 월의 마지막 날짜 가져오기
        if (originalDate > lastDayOfMonth) {
            date.setDate(lastDayOfMonth);  // 만약 원래의 일자가 마지막 날짜보다 크다면, 마지막 날짜로 설정
        } else {
            date.setDate(originalDate);  // 원래의 일자로 다시 설정
        }
        renderCalendar();
    });
});

const flashMessage = document.getElementById('flashMessage').innerText;
if(flashMessage) {
    alert(flashMessage);
}