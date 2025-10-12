var isLoading = false;
var clickDebounce = {};

function debounceClick(selector, callback, delay = 500) {
    if (clickDebounce[selector]) return;
    clickDebounce[selector] = true;
    setTimeout(() => { clickDebounce[selector] = false; }, delay);
    callback();
}

$(document).ready(function () {
    $(document).off('click.scheduled submit.scheduled');

    $(document).on('click.scheduled', '#addIncomeBtn', function (e) {
        e.preventDefault();
        if (isLoading || clickDebounce['#addIncomeBtn']) return;
        debounceClick('#addIncomeBtn', function () {
            isLoading = true;
            $.get(contextPath + '/scheduled_transactions?action=new&type=income')
                .done(function (data) {
                    $('#content').html(data);
                })
                .fail(function (xhr) {
                    console.error('Lỗi load form income:', xhr.responseText);
                })
                .always(() => isLoading = false);
        });
    });

    $(document).on('click.scheduled', '#addExpenseBtn', function (e) {
        e.preventDefault();
        if (isLoading || clickDebounce['#addExpenseBtn']) return;
        debounceClick('#addExpenseBtn', function () {
            isLoading = true;
            $.get(contextPath + '/scheduled_transactions?action=new&type=expense')
                .done(function (data) {
                    $('#content').html(data);
                })
                .fail(function (xhr) {
                    console.error('Lỗi load form expense:', xhr.responseText);
                })
                .always(() => isLoading = false);
        });
    });

    $(document).on('click.scheduled', '#cancelNew', function (e) {
        e.preventDefault();
        if (isLoading || clickDebounce['#cancelNew']) return;
        debounceClick('#cancelNew', function () {
            loadList(true);
        });
    });

    $(document).on('submit.scheduled', '#filterForm', function (e) {
        e.preventDefault();
        if (isLoading || clickDebounce['#filterForm']) return;

        debounceClick('#filterForm', function () {
            isLoading = true;
            var form = $('#filterForm');
            var formData = form.serialize();

            if (!formData || formData.trim() === '') {
                formData = 'action=list';
                form.find('input[type="checkbox"]:checked').each(function () {
                    formData += '&types=' + $(this).val();
                });
                form.find('input[type="text"], input[type="date"], select').each(function () {
                    if ($(this).val()) {
                        formData += '&' + $(this).attr('name') + '=' + $(this).val();
                    }
                });
            }

            console.log('Gửi formData:', formData);

            $.ajax({
                url: form.attr('action'),
                data: formData,
                headers: { "X-Requested-With": "XMLHttpRequest" },
                success: function (data) {
                    $('#content').html(data);
                },
                error: function (xhr) {
                    console.error('Lỗi filter:', xhr.responseText);
                },
                complete: function () {
                    isLoading = false;
                }
            });
        });
    });

    $(document).on('click.scheduled', '#resetFilter', function (e) {
        e.preventDefault();
        if (isLoading || clickDebounce['#resetFilter']) return;

        debounceClick('#resetFilter', function () {
            var form = $('#filterForm');
            form[0].reset();
            form.find('input[type="checkbox"]').prop('checked', false);

            isLoading = true;
            $.ajax({
                url: contextPath + '/scheduled_transactions?action=list',
                headers: { "X-Requested-With": "XMLHttpRequest" },
                success: function (data) {
                    $('#content').html(data);
                },
                error: function (xhr) {
                    console.error('Lỗi reset filter:', xhr.responseText);
                },
                complete: function () {
                    isLoading = false;
                }
            });
        });
    });
});

function loadList(force = false) {
    if (isLoading && !force) {
        console.log('Skip loadList vì đang loading');
        return;
    }
    isLoading = true;
    console.log('Loading list...');

    $.ajax({
        url: contextPath + '/scheduled_transactions?action=list',
        headers: { "X-Requested-With": "XMLHttpRequest" },
        success: function (data) {
            $('#content').html(data);
        },
        error: function (xhr) {
            console.error('Lỗi load list:', xhr.responseText);
        },
        complete: function () {
            clickDebounce = {};
            isLoading = false;
        }
    });
}

function deleteTransaction(id) {
    if (!confirm('Xác nhận xóa giao dịch?') || isLoading || clickDebounce['delete_' + id]) return;
    debounceClick('delete_' + id, function () {
        isLoading = true;
        $.get(contextPath + '/scheduled_transactions?action=delete&id=' + id)
            .done(function () {
                loadList(true);
                console.log('Xóa thành công ID:', id);
            })
            .fail(function (xhr) {
                console.error('Lỗi xóa:', xhr.responseText);
                alert('Lỗi xóa!');
            })
            .always(() => isLoading = false);
    });
}

function skipTransaction(id) {
    if (!confirm('Bỏ qua lần lặp tiếp theo?') || isLoading || clickDebounce['skip_' + id]) return;
    debounceClick('skip_' + id, function () {
        isLoading = true;
        $.post(contextPath + '/scheduled_transactions?action=skip&id=' + id)
            .done(function () {
                loadList(true);
                console.log('Skip thành công ID:', id);
            })
            .fail(function (xhr) {
                console.error('Lỗi skip:', xhr.responseText);
                alert('Lỗi skip!');
            })
            .always(() => isLoading = false);
    });
}
